// ============================================================================
// Portfolio Manager
// File: .github/scripts/pr-autofill.js
// Version: 1.1.0
// Description: Fills the auto-generated parts of the Pull Requests (description,
//              assignee, labels and milestone):
//              work branch -> develop, develop -> qa (promotion) and
//              qa -> main (release)
// Author: Erickson Vergara
// Repository: https://github.com/ericksonvergara/portfolio-manager
// License: MIT
// ============================================================================
//
// Only the content between <!-- auto:<name> --> and <!-- /auto:<name> --> is
// rewritten, plus a few checklist boxes that can be verified through the API.
// Everything else in the description stays as the author left it, and boxes
// ticked by hand inside a block stay ticked when the block is regenerated.

const FLOWS = {
  develop: { kind: 'work', template: null },
  qa: { kind: 'promotion', head: 'develop', template: 'promotion.md' },
  main: { kind: 'release', head: 'qa', template: 'release.md' },
}

const TEMPLATE_DIR = '.github/PULL_REQUEST_TEMPLATE'
const DEFAULT_TEMPLATE = '.github/pull_request_template.md'
const DEFAULT_TEMPLATE_HINT = 'Provide a clear and concise description'
const ROADMAP = 'docs/Sprint_Development_Roadmap.md'
const SMOKE_JOB = 'Smoke Test - Docker & Postman'
const LONG_LIVED = ['develop', 'qa', 'main']
const MIGRATION = /^backend\/src\/main\/resources\/db\/migration\/(V[^/]+\.sql)$/
const UNIT_TEST = /^backend\/src\/test\/.+(?<!IntegrationTest)\.java$|\.test\.tsx?$/
const INTEGRATION_TEST = /^backend\/src\/test\/.+IntegrationTest\.java$|^postman\//

// Labels added from the files changed; only labels that already exist in the repository are used.
const FILE_LABELS = [
  { label: 'backend', files: /^backend\// },
  { label: 'frontend', files: /^frontend\// },
  { label: 'database', files: /^backend\/src\/main\/resources\/db\// },
  { label: 'docker', files: /(^|\/)Dockerfile$|^docker-compose\.yaml$/ },
  { label: 'nginx', files: /^nginx\// },
  { label: 'ci', files: /^\.github\/workflows\// },
  { label: 'documentation', files: /^docs\/|\.md$/ },
  { label: 'testing', files: /^backend\/src\/test\/|\.test\.tsx?$|^postman\// },
]

// A type is ticked by a conventional prefix (PR titles, commits) or by the files changed.
const TYPES = [
  { label: '✨ New Feature', prefixes: ['feat'] },
  { label: '🐞 Bug Fix', prefixes: ['fix', 'hotfix'] },
  { label: '♻️ Refactoring', prefixes: ['refactor'] },
  { label: '📚 Documentation', prefixes: ['docs'], files: /^docs\/|\.md$/ },
  { label: '⚡ Performance Improvement', prefixes: ['perf'] },
  { label: '🔒 Security', prefixes: ['security'] },
  {
    label: '🛠️ Build / DevOps',
    prefixes: ['chore', 'ci', 'build', 'deps'],
    files:
      /^\.github\/workflows\/|(^|\/)Dockerfile$|^docker-compose\.yaml$|^nginx\/|^\.env\.example$/,
  },
  {
    label: '🧪 Tests',
    prefixes: ['test'],
    files: /^backend\/src\/test\/|\.test\.tsx?$|^postman\//,
  },
]

module.exports = async ({ github, context, core }) => {
  const { owner, repo } = context.repo
  const api = { github, owner, repo, core }

  if (context.eventName === 'pull_request') {
    await fill(api, context.payload.pull_request, null)
    return
  }

  // workflow_run: the CI finished; update every open PR built on that commit.
  const run = context.payload.workflow_run
  const pulls = await github.paginate(github.rest.pulls.list, {
    owner,
    repo,
    state: 'open',
    per_page: 100,
  })
  for (const pr of pulls.filter((p) => p.head.sha === run.head_sha)) {
    await fill(api, pr, run)
  }
}

async function fill(api, pr, run) {
  const flow = FLOWS[pr.base.ref]
  const isFlow =
    flow && (flow.kind === 'work' ? !LONG_LIVED.includes(pr.head.ref) : flow.head === pr.head.ref)
  if (!isFlow) {
    api.core.info(`#${pr.number}: ${pr.head.ref} -> ${pr.base.ref} is not handled.`)
    return
  }

  const original = (pr.body || '').replace(/\r\n/g, '\n')
  // An empty description or the untouched default template gets the template of its flow.
  let body = original
  const isPlaceholder = !body.trim() || body.includes(DEFAULT_TEMPLATE_HINT)
  if (flow.template && isPlaceholder) {
    body = await loadFile(api, `${TEMPLATE_DIR}/${flow.template}`, pr.head.sha)
  } else if (!body.includes('<!-- auto:')) {
    if (!isPlaceholder) {
      api.core.notice(`#${pr.number}: custom description without auto blocks, left unchanged.`)
      return
    }
    body = await loadFile(api, DEFAULT_TEMPLATE, pr.head.sha)
  }

  // A pull_request event means new commits: the CI result arrives later through workflow_run.
  const ci = run ? await ciResult(api, run) : null
  const data =
    flow.kind === 'work' ? await collectWork(api, pr) : await collectPromotion(api, pr, flow)
  body = flow.kind === 'work' ? renderWork(body, data, ci) : renderPromotion(body, data, ci, flow)

  await fillSidebar(api, pr, flow, data)

  let title = pr.title
  if (data.version) {
    body = body.replace(/vX\.Y\.Z/g, data.version)
    title = title.replace(/vX\.Y\.Z/g, data.version)
  }

  if (body === original && title === pr.title) {
    api.core.info(`#${pr.number}: description already up to date.`)
    return
  }
  await api.github.rest.pulls.update({
    owner: api.owner,
    repo: api.repo,
    pull_number: pr.number,
    body,
    title,
  })
  api.core.info(`#${pr.number}: description updated.`)
}

// ---------------------------------------------------------------------------
// Data collection
// ---------------------------------------------------------------------------

async function collectWork(api, pr) {
  const { github, owner, repo } = api
  const [files, commits, [pull], compare] = await Promise.all([
    github.paginate(github.rest.pulls.listFiles, {
      owner,
      repo,
      pull_number: pr.number,
      per_page: 100,
    }),
    github.paginate(github.rest.pulls.listCommits, {
      owner,
      repo,
      pull_number: pr.number,
      per_page: 100,
    }),
    fetchPulls(api, [pr.number]),
    github.rest.repos.compareCommitsWithBasehead({
      owner,
      repo,
      basehead: `${pr.base.sha}...${pr.head.sha}`,
      per_page: 1,
    }),
  ])
  const messages = commits.map((c) => c.commit.message)
  return {
    pull,
    files,
    commitTypes: messages.map(conventionalType).filter(Boolean),
    breaking: breakingChanges(files, [pull.title, ...messages]),
    upToDate: await isUpToDate(api, pr, compare.data),
  }
}

async function collectPromotion(api, pr, flow) {
  const compare = await compareAll(api, pr.base.ref, pr.head.ref)
  const merges = compare.commits
    .map((c) => c.commit.message.match(/^Merge pull request #(\d+) from [^/\s]+\/(\S+)/))
    .filter(Boolean)
    .map((m) => ({ number: Number(m[1]), branch: m[2] }))

  const included = merges.filter((m) => !LONG_LIVED.includes(m.branch)).map((m) => m.number)
  const promotions = merges.filter((m) => m.branch === 'develop').map((m) => m.number)
  const promotion = flow.kind === 'release' && promotions.length ? Math.max(...promotions) : null
  const [pulls, [promotionPull]] = await Promise.all([
    fetchPulls(api, included),
    fetchPulls(api, promotion ? [promotion] : []),
  ])

  const breaking = breakingChanges(compare.files, [
    ...pulls.map((p) => p.title),
    ...compare.commits.map((c) => c.commit.message),
  ])
  return {
    pulls,
    headSha: pr.head.sha,
    promotion: promotionPull || null,
    files: compare.files,
    sprints: sprintsOf(await loadFile(api, ROADMAP, pr.head.sha), pulls),
    breaking,
    criticalBugs: flow.kind === 'release' ? await openCriticalBugs(api) : 0,
    version: await nextVersion(api, pulls.some((p) => p.type === 'feat') || breaking.length > 0),
  }
}

async function compareAll(api, base, head) {
  const commits = []
  let files = []
  for (let page = 1; ; page++) {
    const { data } = await api.github.rest.repos.compareCommitsWithBasehead({
      owner: api.owner,
      repo: api.repo,
      basehead: `${base}...${head}`,
      per_page: 100,
      page,
    })
    if (page === 1) files = data.files || []
    commits.push(...data.commits)
    if (data.commits.length < 100 || commits.length >= data.total_commits) break
  }
  return { commits, files }
}

// The base branch may have moved since the PR was opened: compare against its current tip.
async function isUpToDate(api, pr, compareFromPrBase) {
  const { data: base } = await api.github.rest.repos.getBranch({
    owner: api.owner,
    repo: api.repo,
    branch: pr.base.ref,
  })
  if (base.commit.sha === pr.base.sha) return compareFromPrBase.behind_by === 0
  const { data } = await api.github.rest.repos.compareCommitsWithBasehead({
    owner: api.owner,
    repo: api.repo,
    basehead: `${base.commit.sha}...${pr.head.sha}`,
    per_page: 1,
  })
  return data.behind_by === 0
}

async function fetchPulls(api, numbers) {
  if (!numbers.length) return []
  const fields = `number title headRefName author { login } labels(first: 20) { nodes { name } }
    closingIssuesReferences(first: 10) {
      nodes { number title labels(first: 20) { nodes { name } } milestone { number title } }
    }
    body mergeCommit { oid }
    commits(last: 1) { nodes { commit { statusCheckRollup { state } } } }`
  const query = `query($owner: String!, $repo: String!) { repository(owner: $owner, name: $repo) {
    ${numbers.map((n) => `pr${n}: pullRequest(number: ${n}) { ${fields} }`).join('\n')}
  } }`
  const data = await api.github.graphql(query, { owner: api.owner, repo: api.repo })
  return numbers
    .map((n) => data.repository[`pr${n}`])
    .filter(Boolean)
    .sort((a, b) => a.number - b.number)
    .map(describePull)
}

function describePull(pr) {
  const conventional = pr.title.match(/^(\w+)(?:\(([^)]+)\))?!?:\s*(.+)$/)
  const story =
    (pr.headRefName.match(/^(?:feature|fix|hotfix)\/([A-Z]+\d+)-/) || [])[1] ||
    (conventional && /^[A-Z]+\d+$/.test(conventional[2] || '') ? conventional[2] : null)
  const issues = new Map(pr.closingIssuesReferences.nodes.map((i) => [i.number, i.title]))
  for (const m of (pr.body || '').matchAll(
    /\b(?:close[sd]?|fix(?:e[sd])?|resolve[sd]?|refs?)\s+#(\d+)/gi,
  )) {
    if (!issues.has(Number(m[1]))) issues.set(Number(m[1]), null)
  }
  const labels = pr.labels.nodes.map((l) => l.name.toLowerCase())
  const dependabot = pr.author?.login === 'dependabot' || pr.headRefName.startsWith('dependabot/')
  const rollup = pr.commits.nodes[0]?.commit.statusCheckRollup?.state || null
  return {
    number: pr.number,
    title: pr.title,
    summary: conventional ? conventional[3] : pr.title,
    type: dependabot
      ? 'deps'
      : labels.includes('security')
        ? 'security'
        : conventionalType(pr.title),
    story,
    storyTitle: [...issues.values()].find(Boolean) || null,
    issues: [...issues.keys()],
    dependabot,
    labels: pr.labels.nodes.map((l) => l.name),
    issueLabels: pr.closingIssuesReferences.nodes.flatMap((i) => i.labels.nodes.map((l) => l.name)),
    issueMilestone: pr.closingIssuesReferences.nodes.find((i) => i.milestone)?.milestone || null,
    ciGreen: rollup === 'SUCCESS',
    mergeCommit: pr.mergeCommit?.oid || null,
  }
}

function conventionalType(message) {
  const m = message.match(/^(\w+)(?:\([^)]*\))?!?:/)
  return m ? m[1].toLowerCase() : null
}

async function ciResult(api, run) {
  const { data } = await api.github.rest.actions.listJobsForWorkflowRun({
    owner: api.owner,
    repo: api.repo,
    run_id: run.id,
    per_page: 100,
  })
  const jobs = data.jobs.map((j) => ({ name: j.name, conclusion: j.conclusion }))
  return {
    url: `https://github.com/${api.owner}/${api.repo}/actions/runs/${run.id}`,
    jobs,
    smoke: jobs.find((j) => j.name === SMOKE_JOB) || null,
    passed: run.conclusion === 'success',
  }
}

// New features (or breaking changes while in 0.x) bump the minor version; anything else, the patch.
async function nextVersion(api, minor) {
  try {
    const { data } = await api.github.rest.repos.getLatestRelease({
      owner: api.owner,
      repo: api.repo,
    })
    const m = data.tag_name.match(/^v?(\d+)\.(\d+)\.(\d+)/)
    if (m) return minor ? `v${m[1]}.${Number(m[2]) + 1}.0` : `v${m[1]}.${m[2]}.${Number(m[3]) + 1}`
  } catch (error) {
    if (error.status !== 404) throw error
  }
  return 'v0.1.0'
}

async function openCriticalBugs(api) {
  const { data } = await api.github.rest.issues.listForRepo({
    owner: api.owner,
    repo: api.repo,
    state: 'open',
    labels: 'bug,critical',
    per_page: 100,
  })
  return data.filter((i) => !i.pull_request).length
}

async function loadFile(api, path, ref) {
  const { data } = await api.github.rest.repos.getContent({
    owner: api.owner,
    repo: api.repo,
    path,
    ...(ref ? { ref } : {}),
  })
  return Buffer.from(data.content, 'base64').toString('utf8').replace(/\r\n/g, '\n')
}

// Sprints of the roadmap ("# Sprint 3 - Landing Page", "**Objetivo:** ...", "* HU001") that
// contain the included stories, most stories first.
function sprintsOf(roadmap, pulls) {
  const ids = new Set(pulls.map((p) => p.story).filter(Boolean))
  return roadmap
    .split(/^(?=#{1,2} Sprint \d+)/m)
    .map((section) => {
      const heading = section.match(/^#{1,2} Sprint (\d+)\s*-\s*(.+)$/m)
      if (!heading) return null
      const goal = (section.match(/\*\*Objetivo:\*\*\s*(.+)/) || [])[1]
      const stories = [...section.matchAll(/^\* ((?:HU|HT)\d+)/gm)].map((m) => m[1])
      const hits = stories.filter((s) => ids.has(s))
      return {
        number: heading[1],
        name: heading[2].trim(),
        goal: goal?.trim().replace(/\.$/, ''),
        hits,
      }
    })
    .filter((s) => s && s.hits.length)
    .sort((a, b) => b.hits.length - a.hits.length)
}

function breakingChanges(files, messages) {
  const reasons = []
  for (const message of messages) {
    if (/^\w+(\([^)]*\))?!:/.test(message) || /^BREAKING[ -]CHANGE:/m.test(message)) {
      reasons.push(`Marked as breaking: \`${message.split('\n')[0]}\`.`)
    }
  }
  for (const key of envKeys(files).removed) reasons.push(`Removed environment variable \`${key}\`.`)
  for (const f of files) {
    const migration = f.filename.match(MIGRATION)
    if (
      migration &&
      /^\+.*\b(DROP\s+(TABLE|COLUMN)|RENAME\s+(TO|COLUMN))\b/im.test(f.patch || '')
    ) {
      reasons.push(`Migration \`${migration[1]}\` drops or renames tables or columns.`)
    }
  }
  return [...new Set(reasons)]
}

function envKeys(files) {
  const env = files.find((f) => f.filename === '.env.example')
  const keys = (sign) =>
    new Set(
      [...(env?.patch || '').matchAll(new RegExp(`^\\${sign}([A-Z][A-Z0-9_]*)=`, 'gm'))].map(
        (m) => m[1],
      ),
    )
  const plus = keys('+')
  const minus = keys('-')
  return {
    added: [...plus].filter((k) => !minus.has(k)),
    removed: [...minus].filter((k) => !plus.has(k)),
  }
}

// ---------------------------------------------------------------------------
// Sidebar: assignee, labels and milestone
// ---------------------------------------------------------------------------

// Only adds: labels are never removed, and the assignee and milestone are set only when empty.
async function fillSidebar(api, pr, flow, data) {
  const { github, owner, repo } = api
  const issue = { owner, repo, issue_number: pr.number }

  const author = pr.user?.login
  if (!pr.assignees?.length && author && pr.user.type !== 'Bot') {
    await github.rest.issues.addAssignees({ ...issue, assignees: [author] })
    api.core.info(`#${pr.number}: assigned to ${author}.`)
  }

  const existing = new Set(
    (
      await github.paginate(github.rest.issues.listLabelsForRepo, { owner, repo, per_page: 100 })
    ).map((l) => l.name),
  )
  const current = new Set((pr.labels || []).map((l) => l.name))
  const files = data.files.map((f) => f.filename)
  const wanted = [
    ...(flow.kind === 'work'
      ? data.pull.issueLabels
      : data.pulls.flatMap((p) => [...p.labels, ...p.issueLabels])),
    ...FILE_LABELS.filter(({ files: re }) => files.some((f) => re.test(f))).map((l) => l.label),
  ]
  const missing = [...new Set(wanted)].filter((l) => existing.has(l) && !current.has(l))
  if (missing.length) {
    await github.rest.issues.addLabels({ ...issue, labels: missing })
    api.core.info(`#${pr.number}: labels added: ${missing.join(', ')}.`)
  }

  if (pr.milestone) return
  let milestone = flow.kind === 'work' ? data.pull.issueMilestone : null
  if (flow.kind !== 'work' && data.sprints[0]) {
    const title = `Sprint ${data.sprints[0].number}`
    const milestones = await github.paginate(github.rest.issues.listMilestones, {
      owner,
      repo,
      state: 'all',
      per_page: 100,
    })
    milestone = milestones.find((m) => m.title === title) || null
  }
  if (milestone) {
    await github.rest.issues.update({ ...issue, milestone: milestone.number })
    api.core.info(`#${pr.number}: milestone ${milestone.title}.`)
  }
}

// ---------------------------------------------------------------------------
// Rendering: work branch -> develop
// ---------------------------------------------------------------------------

function renderWork(body, data, ci) {
  const { pull, files } = data

  body = replaceBlock(body, 'issues', () => {
    if (!pull.issues.length) return 'No related Issue (repository maintenance).'
    const titles = pull.storyTitle
      ? `\n\nRelated Issues:\n\n- ${pull.story || `#${pull.issues[0]}`} — ${pull.storyTitle}`
      : ''
    return `${pull.issues.map((n) => `Closes #${n}`).join('\n')}${titles}`
  })

  body = replaceBlock(body, 'types', (inner) =>
    renderTypes(inner, [pull.type, ...data.commitTypes], files),
  )

  body = replaceBlock(body, 'testing', (inner) => {
    const ticked = checkedKeys(inner, (text) => text)
    const box = (label, auto) => `- [${auto || ticked.has(label) ? 'x' : ' '}] ${label}`
    return [
      box(
        'Unit Tests',
        files.some((f) => UNIT_TEST.test(f.filename)),
      ),
      box(
        'Integration Tests',
        files.some((f) => INTEGRATION_TEST.test(f.filename)),
      ),
      box('Manual Tests', false),
      box('Regression Tests', ci?.passed && ci.smoke?.conclusion === 'success'),
    ].join('\n')
  })

  body = replaceBlock(body, 'evidence', () => renderCiJobs(ci))
  body = replaceBlock(body, 'breaking', (inner) => renderBreaking(inner, data.breaking))
  body = replaceBlock(body, 'deploy', () => deploymentNotes(files).join('\n'))

  body = setBox(body, 'Tests executed successfully.', Boolean(ci?.passed))
  body = setBox(body, 'Related Issue(s) linked.', pull.issues.length > 0)
  body = setBox(body, 'Branch is up to date.', data.upToDate)
  return body
}

// ---------------------------------------------------------------------------
// Rendering: develop -> qa and qa -> main
// ---------------------------------------------------------------------------

function renderPromotion(body, data, ci, flow) {
  const stories = data.pulls.filter((p) => p.story)
  const others = data.pulls.filter((p) => !p.story && !p.dependabot)
  const deps = data.pulls.filter((p) => p.dependabot)
  const issues = [...new Set(data.pulls.flatMap((p) => p.issues))].sort((a, b) => a - b)
  const added = data.files.reduce((sum, f) => sum + f.additions, 0)
  const removed = data.files.reduce((sum, f) => sum + f.deletions, 0)
  const fileCount = `${data.files.length}${data.files.length >= 300 ? '+' : ''}`
  const [sprint] = data.sprints

  body = replaceBlock(body, 'sprint', () => {
    if (!sprint) return '**Maintenance** – no roadmap stories in this version.'
    const extra = data.sprints.slice(1).map((s) => `Sprint ${s.number} (${s.hits.join(', ')})`)
    return [
      `**Sprint ${sprint.number} – ${sprint.name}**${sprint.goal ? `: ${sprint.goal}` : '.'}`,
      ...(extra.length ? ['', `Also includes stories from ${extra.join(', ')}.`] : []),
    ].join('\n')
  })

  body = replaceBlock(body, 'objective', () => {
    const goal = sprint
      ? `the Sprint ${sprint.number} goal (${sprint.goal || sprint.name})`
      : 'the maintenance changes'
    return flow.kind === 'release'
      ? `Release ${goal} to \`main\`. The work was validated in QA and is ready for production.`
      : `Validate ${goal} in QA before releasing to \`main\` as **vX.Y.Z**.`
  })

  body = replaceBlock(body, 'prs', () => {
    const rows = [
      ...stories.map((p) => `| #${p.number} | **${p.story}** – ${p.storyTitle || p.summary} |`),
      ...others.map((p) => `| #${p.number} | ${p.title} |`),
    ]
    if (deps.length)
      rows.push(`| ${deps.map((p) => `#${p.number}`).join(', ')} | Dependencies (Dependabot) |`)
    if (!rows.length) rows.push('| – | No merged Pull Requests found |')
    return [
      '| PR | Change |',
      '|----|--------|',
      ...rows,
      '',
      `${fileCount} files changed, +${added} / −${removed}.`,
    ].join('\n')
  })

  body = replaceBlock(body, 'stories', () => {
    const lines = stories.map(
      (p) => `- **${p.storyTitle || p.summary} (${p.story}, #${p.number}):** ${p.summary}`,
    )
    const extra = [
      ...others.map((p) => `- ${p.title} (#${p.number})`),
      ...(deps.length
        ? [`- Dependency updates (${deps.map((p) => `#${p.number}`).join(', ')})`]
        : []),
    ]
    if (extra.length) lines.push('', 'Also included:', '', ...extra)
    return lines.join('\n') || '- No stories found.'
  })

  body = replaceBlock(body, 'promotion', () =>
    data.promotion
      ? `Validated in \`qa\` through #${data.promotion.number}.`
      : 'Validated in `qa` through #',
  )

  body = replaceBlock(body, 'issues', () => {
    const lines = [
      issues.length ? `Refs ${issues.map((n) => `#${n}`).join(', ')}` : 'No related Issues.',
    ]
    if (data.promotion) lines.push('', `Promotion to QA: #${data.promotion.number}`)
    return lines.join('\n')
  })

  body = replaceBlock(body, 'types', (inner) =>
    renderTypes(
      inner,
      data.pulls.map((p) => p.type),
      data.files,
    ),
  )

  body = replaceBlock(body, 'acceptance', (inner) => {
    const ticked = checkedKeys(inner, (text) => (text.match(/^([A-Z]+\d+)/) || [])[1])
    if (!stories.length) return '  - [ ] No stories in this version.'
    return stories
      .map((p) => {
        const issue = p.issues.length ? ` (#${p.issues[0]})` : ''
        return `  - [${ticked.has(p.story) ? 'x' : ' '}] ${p.story} – ${p.storyTitle || p.summary}${issue}`
      })
      .join('\n')
  })

  body = replaceBlock(body, 'ci', () => renderSmoke(ci))
  body = replaceBlock(body, 'breaking', (inner) => renderBreaking(inner, data.breaking))
  body = replaceBlock(body, 'deploy', () => deploymentNotes(data.files).join('\n'))

  const storiesLinked = stories.every((p) => p.issues.length > 0)
  if (flow.kind === 'promotion') {
    body = setBox(
      body,
      'Every included PR is merged into `develop` with CI green.',
      data.pulls.every((p) => p.ciGreen),
    )
    body = setBox(body, 'Related Issue(s) linked with `Refs`.', storiesLinked)
  } else {
    body = setBox(
      body,
      '`qa` is identical to what was tested (no merges after QA).',
      Boolean(data.promotion?.mergeCommit) && data.promotion.mergeCommit === data.headSha,
    )
    body = setBox(body, 'No known critical defects remain.', data.criticalBugs === 0)
    body = setBox(body, 'Every included story is linked with `Refs #`.', storiesLinked)
    body = setBox(body, 'Version number decided (vX.Y.Z).', true)
  }
  return body
}

// ---------------------------------------------------------------------------
// Shared blocks
// ---------------------------------------------------------------------------

function renderTypes(inner, prefixes, files) {
  const ticked = checkedKeys(inner, (text) => text)
  return TYPES.map((type) => {
    const auto =
      prefixes.some((p) => type.prefixes.includes(p)) ||
      Boolean(type.files && files.some((f) => type.files.test(f.filename)))
    return `- [${auto || ticked.has(type.label) ? 'x' : ' '}] ${type.label}`
  }).join('\n')
}

function renderBreaking(inner, reasons) {
  const manual = /^\s*- \[[xX]\]/m.test(inner.split('\n')[0] || '')
  const box = `- [${reasons.length || manual ? 'x' : ' '}] This Pull Request introduces breaking changes.`
  if (!reasons.length) {
    return `${box}\n\nNone detected (breaking markers, removed variables, dropped or renamed columns).`
  }
  return [box, '', 'Detected:', '', ...reasons.map((r) => `- ${r}`)].join('\n')
}

function renderCiJobs(ci) {
  if (!ci) return '- CI: waiting for the run of the latest commit.'
  const icon = (c) => (c === 'success' ? '✅' : c === 'skipped' ? '⏭️' : '❌')
  return [
    `- [CI run](${ci.url}): ${ci.passed ? 'all jobs passed' : '**failed**'}.`,
    ...ci.jobs.map((j) => `  - ${icon(j.conclusion)} ${j.name}`),
    ...(ci.smoke?.conclusion === 'success'
      ? [
          '- Regression: the Postman collection (authentication, public content and documentation) passed against the full Docker stack.',
        ]
      : []),
  ].join('\n')
}

function renderSmoke(ci) {
  if (!ci) {
    return [
      '- [ ] Smoke test of the full stack (`docker compose up --build`): waiting for the CI run.',
      '- [ ] Postman collection with Newman: waiting for the CI run.',
      '- [ ] API regression of the previous release (Postman): waiting for the CI run.',
    ].join('\n')
  }
  const passed = ci.smoke?.conclusion === 'success'
  const link = `[CI run](${ci.url})`
  const result = !ci.smoke
    ? `job *${SMOKE_JOB}* not found in the ${link}`
    : passed
      ? `passed in the ${link}`
      : `**${ci.smoke.conclusion || 'did not finish'}** in the ${link}`
  const box = passed ? 'x' : ' '
  return [
    `- [${box}] Smoke test of the full stack (\`docker compose up --build\`): ${result}.`,
    `- [${box}] Postman collection with Newman: ${result}.`,
    `- [${box}] API regression of the previous release (Postman: authentication, public content, documentation): ${result}.`,
  ].join('\n')
}

function deploymentNotes(files) {
  const notes = []
  for (const f of files) {
    const migration = f.filename.match(MIGRATION)
    if (migration && f.status === 'added') {
      notes.push(`- New Flyway migration \`${migration[1]}\`, applied automatically on startup.`)
    }
  }
  const env = envKeys(files)
  for (const key of env.added)
    notes.push(`- New environment variable \`${key}\` (see \`.env.example\`).`)
  for (const key of env.removed) notes.push(`- Removed environment variable \`${key}\`.`)
  for (const f of files) {
    if (!/(^|\/)Dockerfile$/.test(f.filename) || !f.patch) continue
    for (const m of f.patch.matchAll(/^\+FROM\s+(\S+)/gm))
      notes.push(`- \`${f.filename}\` base image: \`${m[1]}\`.`)
  }
  if (files.some((f) => f.filename === 'frontend/.nvmrc'))
    notes.push('- Node.js version changed (`frontend/.nvmrc`).')
  const gradle = files.find((f) => f.filename === 'backend/build.gradle')
  if (gradle && /^\+.*languageVersion/m.test(gradle.patch || ''))
    notes.push('- Java version changed (`backend/build.gradle`).')
  if (files.some((f) => f.filename === 'docker-compose.yaml'))
    notes.push('- `docker-compose.yaml` changed: review services and variables.')
  return notes.length ? notes : ['- No new migrations, environment variables or runtime changes.']
}

// ---------------------------------------------------------------------------
// Blocks and boxes
// ---------------------------------------------------------------------------

function replaceBlock(body, name, renderInner) {
  const block = new RegExp(
    `([ \\t]*<!-- auto:${name} -->)\\n?([\\s\\S]*?)\\n?([ \\t]*<!-- /auto:${name} -->)`,
    'g',
  )
  return body.replace(block, (_, open, inner, close) => `${open}\n${renderInner(inner)}\n${close}`)
}

function checkedKeys(inner, keyOf) {
  return new Set(
    [...inner.matchAll(/^\s*- \[[xX]\]\s+(.+)$/gm)].map((m) => keyOf(m[1].trim())).filter(Boolean),
  )
}

// Ticks a checklist line verified through the API. A box already ticked by hand stays ticked.
function setBox(body, text, verified) {
  if (!verified) return body
  const escaped = text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return body.replace(new RegExp(`^(\\s*- )\\[ \\]( ${escaped})$`, 'm'), '$1[x]$2')
}

module.exports.internals = {
  fill,
  renderWork,
  renderPromotion,
  sprintsOf,
  breakingChanges,
  deploymentNotes,
  replaceBlock,
}
