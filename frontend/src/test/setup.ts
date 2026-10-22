import '@testing-library/jest-dom/vitest'

// jsdom has no object URLs; the image previews (HU020) need them.
let objectUrls = 0
URL.createObjectURL = () => `blob:preview-${++objectUrls}`
URL.revokeObjectURL = () => {}
