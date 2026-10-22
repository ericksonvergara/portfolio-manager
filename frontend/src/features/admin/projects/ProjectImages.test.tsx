import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AxiosError, type AxiosResponse } from 'axios'
import { useState } from 'react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { AdminProject } from './adminProjectsApi'
import { ProjectImages } from './ProjectImages'

/* Plain functions instead of vi.fn(), for the same reason as in LoginPage.test.tsx. */
const api = vi.hoisted(() => ({
  calls: [] as string[],
  answer: (): Promise<AdminProject> => Promise.reject(new Error('not set')),
}))

vi.mock('./adminProjectsApi', () => ({
  uploadCover: (id: number, file: File) => {
    api.calls.push(`cover ${id} ${file.name}`)
    return api.answer()
  },
  removeCover: (id: number) => {
    api.calls.push(`remove-cover ${id}`)
    return api.answer()
  },
  addGalleryImage: (id: number, file: File, altText: string) => {
    api.calls.push(`gallery ${id} ${file.name} ${altText}`)
    return api.answer()
  },
  removeGalleryImage: (id: number, imageId: number) => {
    api.calls.push(`remove-image ${id} ${imageId}`)
    return api.answer()
  },
}))

const PROJECT: AdminProject = {
  id: 7,
  slug: 'sistema-erp',
  name: 'Sistema ERP',
  shortDescription: 'Corta',
  description: 'Completa',
  coverImageUrl: null,
  projectUrl: null,
  repositoryUrl: null,
  videoUrl: null,
  status: 'COMPLETED',
  developedOn: null,
  displayOrder: 1,
  featured: false,
  active: false,
  technologies: [],
  gallery: [],
}

function png(name = 'portada.png', size = 10) {
  return new File([new Uint8Array(size)], name, { type: 'image/png' })
}

function Harness({ initial }: { initial: AdminProject }) {
  const [project, setProject] = useState(initial)
  return <ProjectImages project={project} onChange={setProject} />
}

beforeEach(() => {
  api.calls = []
  api.answer = () => Promise.reject(new Error('not set'))
})

describe('ProjectImages', () => {
  it('uploads the cover image', async () => {
    api.answer = () =>
      Promise.resolve({ ...PROJECT, coverImageUrl: '/uploads/a.png' })
    const user = userEvent.setup()
    render(<Harness initial={PROJECT} />)

    expect(screen.getByText(/Sin imagen principal/)).toBeInTheDocument()
    const upload = screen.getByRole('button', { name: 'Subir imagen' })
    expect(upload).toBeDisabled()

    await user.upload(
      screen.getByLabelText('Archivo de la imagen principal'),
      png(),
    )
    await user.click(upload)

    expect(await screen.findByRole('status')).toHaveTextContent(
      'Imagen principal guardada.',
    )
    expect(api.calls).toEqual(['cover 7 portada.png'])
    expect(
      screen.getByRole('img', { name: 'Imagen principal de Sistema ERP' }),
    ).toHaveAttribute('src', '/uploads/a.png')
    expect(
      screen.getByRole('button', { name: 'Reemplazar imagen' }),
    ).toBeDisabled()
  })

  it('rejects other formats and large files before uploading', async () => {
    const user = userEvent.setup({ applyAccept: false })
    render(<Harness initial={PROJECT} />)
    const input = screen.getByLabelText('Archivo de la imagen principal')

    await user.upload(
      input,
      new File(['GIF89a'], 'animacion.gif', { type: 'image/gif' }),
    )
    expect(screen.getByRole('alert')).toHaveTextContent(
      'Formato no permitido. Usa una imagen JPG, JPEG o PNG.',
    )
    expect(screen.getByRole('button', { name: 'Subir imagen' })).toBeDisabled()

    await user.upload(input, png('grande.png', 6 * 1024 * 1024))
    expect(screen.getByRole('alert')).toHaveTextContent(
      'La imagen supera el tamaño máximo de 5 MB.',
    )
    expect(api.calls).toHaveLength(0)
  })

  it('explains a file rejected by the server', async () => {
    api.answer = () =>
      Promise.reject(
        new AxiosError('Bad Request', 'ERR_BAD_REQUEST', undefined, undefined, {
          status: 400,
        } as AxiosResponse),
      )
    const user = userEvent.setup()
    render(<Harness initial={PROJECT} />)
    await user.upload(
      screen.getByLabelText('Archivo de la imagen principal'),
      png(),
    )
    await user.click(screen.getByRole('button', { name: 'Subir imagen' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'El archivo no es una imagen JPG o PNG válida.',
    )
  })

  it('keeps the cover of a published project', () => {
    render(
      <Harness
        initial={{ ...PROJECT, coverImageUrl: '/uploads/a.png', active: true }}
      />,
    )

    expect(screen.getByRole('button', { name: 'Quitar imagen' })).toBeDisabled()
    expect(screen.getByText(/primero\s+desactívalo/)).toBeInTheDocument()
  })

  it('adds a gallery image with its alternative text and deletes it after a confirmation', async () => {
    const withImage = {
      ...PROJECT,
      gallery: [{ id: 12, url: '/uploads/g.png', altText: 'Panel de ventas' }],
    }
    api.answer = () => Promise.resolve(withImage)
    const user = userEvent.setup()
    render(<Harness initial={PROJECT} />)

    await user.upload(
      screen.getByLabelText('Nueva imagen de la galería'),
      png('g.png'),
    )
    await user.type(
      screen.getByLabelText(/Texto alternativo/),
      'Panel de ventas',
    )
    await user.click(
      screen.getByRole('button', { name: 'Agregar a la galería' }),
    )

    expect(
      await screen.findByRole('img', { name: 'Panel de ventas' }),
    ).toBeInTheDocument()
    expect(screen.getByLabelText(/Texto alternativo/)).toHaveValue('')

    api.answer = () => Promise.resolve(PROJECT)
    await user.click(
      screen.getByRole('button', { name: 'Eliminar imagen 1 de la galería' }),
    )
    await user.click(
      within(screen.getByRole('alertdialog')).getByRole('button', {
        name: 'Eliminar',
      }),
    )

    expect(
      await screen.findByText('La galería está vacía.'),
    ).toBeInTheDocument()
    expect(api.calls).toEqual([
      'gallery 7 g.png Panel de ventas',
      'remove-image 7 12',
    ])
  })
})
