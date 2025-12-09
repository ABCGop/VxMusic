import { MetadataRoute } from 'next'

export default function sitemap(): MetadataRoute.Sitemap {
  const baseUrl = 'https://lyrics.simpmusic.org'
  const currentDate = new Date()

  // Define routes with their specific settings
  const routeSettings = [
    {
      path: '',
      changeFrequency: 'daily' as const,
      priority: 1.0,
      lastModified: currentDate
    },
    {
      path: '/search',
      changeFrequency: 'always' as const,
      priority: 0.9,
      lastModified: currentDate
    },
    {
      path: '/api',
      changeFrequency: 'weekly' as const,
      priority: 0.8,
      lastModified: new Date('2024-12-20') // Updated API docs date
    },
    {
      path: '/about',
      changeFrequency: 'monthly' as const,
      priority: 0.7,
      lastModified: new Date('2024-12-20')
    },
    {
      path: '/donate',
      changeFrequency: 'monthly' as const,
      priority: 0.6,
      lastModified: new Date('2024-12-20')
    },
    {
      path: '/privacy-policy',
      changeFrequency: 'yearly' as const,
      priority: 0.5,
      lastModified: new Date('2024-12-20')
    }
  ]

  const staticPages: MetadataRoute.Sitemap = routeSettings.map((route) => ({
    url: `${baseUrl}${route.path}`,
    lastModified: route.lastModified,
    changeFrequency: route.changeFrequency,
    priority: route.priority,
  }))

  return staticPages
}
