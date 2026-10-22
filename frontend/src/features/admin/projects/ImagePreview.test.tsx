import { fireEvent, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { ImagePreview } from './ImagePreview'

function png(name: string, size: number) {
  return new File([new Uint8Array(size)], name, { type: 'image/png' })
}

afterEach(() => {
  vi.restoreAllMocks()
})

describe('ImagePreview', () => {
  it('shows the selected file before it is saved, at its original size', () => {
    render(
      <ImagePreview
        file={png('portada.png', 8 * 1024)}
        label="Vista previa de la imagen principal"
        disabled={false}
        onReplace={() => {}}
        onDiscard={() => {}}
      />,
    )
    const image = screen.getByRole('img', {
      name: 'Vista previa de la imagen principal',
    })
    expect(image.getAttribute('src')).toMatch(/^blob:/)
    expect(image).toHaveClass('object-contain')
    expect(screen.getByText(/portada\.png · 8 KB/)).toBeInTheDocument()

    Object.defineProperty(image, 'naturalWidth', { value: 1920 })
    Object.defineProperty(image, 'naturalHeight', { value: 1080 })
    fireEvent.load(image)

    expect(screen.getByText(/1920 × 1080 px/)).toBeInTheDocument()
    expect(screen.getByText(/Aún no se ha guardado/)).toBeInTheDocument()
  })

  it('lets the administrator replace or discard the selection', async () => {
    const replaced: string[] = []
    const user = userEvent.setup()
    render(
      <ImagePreview
        file={png('portada.png', 10)}
        label="Vista previa"
        disabled={false}
        onReplace={() => replaced.push('replace')}
        onDiscard={() => replaced.push('discard')}
      />,
    )

    await user.click(screen.getByRole('button', { name: 'Cambiar imagen' }))
    await user.click(screen.getByRole('button', { name: 'Descartar' }))

    expect(replaced).toEqual(['replace', 'discard'])
  })

  it('releases the local copy of the file when it is no longer shown', () => {
    const revoke = vi.spyOn(URL, 'revokeObjectURL')
    const props = {
      label: 'Vista previa',
      disabled: false,
      onReplace: () => {},
      onDiscard: () => {},
    }
    const { rerender, unmount } = render(
      <ImagePreview file={png('uno.png', 10)} {...props} />,
    )
    const first = screen.getByRole('img').getAttribute('src')

    rerender(<ImagePreview file={png('dos.png', 10)} {...props} />)
    expect(revoke).toHaveBeenCalledWith(first)

    const second = screen.getByRole('img').getAttribute('src')
    unmount()
    expect(revoke).toHaveBeenCalledWith(second)
  })
})
