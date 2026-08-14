import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import App from './App'

describe('App', () => {
  it('should render the application title', () => {
    render(<App />)

    expect(
      screen.getByRole('heading', {
        name: 'Portfolio Manager',
      }),
    ).toBeInTheDocument()
  })
})
