import '@testing-library/jest-dom/vitest'
import { vi } from 'vitest'

// jsdom has no object URLs; the image previews (HU020) need them.
let objectUrls = 0
URL.createObjectURL = () => `blob:preview-${++objectUrls}`
URL.revokeObjectURL = () => {}

// Opening a public page registers a visit (HU025): no test needs a real request for it.
vi.mock('../features/landing/visitsApi', () => ({
  registerVisit: () => Promise.resolve(),
}))
