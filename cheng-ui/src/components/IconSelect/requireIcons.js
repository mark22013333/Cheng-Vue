// Vite 環境使用 import.meta.glob 替代 require.context
const svgFiles = import.meta.glob('../../assets/icons/svg/*.svg', { eager: true })

const re = /.*\/(.*)\.svg$/

const icons = Object.keys(svgFiles).map(path => {
  const match = path.match(re)
  return match ? match[1] : ''
}).filter(Boolean)

export default icons
