export const slugify = (s: string) =>
  s
    .toLowerCase()
    .replace(/https?:\/\//, '')
    .replace(/[^\w]+/g, '-')
    .replace(/^-+|-+$/g, '');