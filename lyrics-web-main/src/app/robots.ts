import { MetadataRoute } from 'next'

export default function robots(): MetadataRoute.Robots {
    const baseUrl = 'https://lyrics.simpmusic.org'

    return {
        rules: [
            {
                userAgent: '*',
                allow: '/',
                disallow: ['/api/private/', '/admin/'],
            },
            {
                userAgent: 'Googlebot',
                allow: '/',
                disallow: ['/api/private/', '/admin/'],
            },
            {
                userAgent: 'Bingbot',
                allow: '/',
                disallow: ['/api/private/', '/admin/'],
            },
        ],
        sitemap: `${baseUrl}/sitemap.xml`,
        host: baseUrl,
    }
}
