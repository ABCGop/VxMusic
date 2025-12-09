import { MetadataRoute } from 'next'

export default function manifest(): MetadataRoute.Manifest {
    return {
        name: 'SimpMusic Lyrics',
        short_name: 'SimpLyrics',
        description: 'Discover synced lyrics and translations for your favorite songs. Free lyrics database with YouTube Music integration.',
        start_url: '/',
        display: 'standalone',
        background_color: '#ffffff',
        theme_color: '#000000',
        icons: [
            {
                src: '/logo_simpmusic.png',
                sizes: 'any',
                type: 'image/png',
            },
            {
                src: '/logo_simpmusic.png',
                sizes: '192x192',
                type: 'image/png',
            },
            {
                src: '/logo_simpmusic.png',
                sizes: '512x512',
                type: 'image/png',
            },
        ],
        categories: ['music', 'entertainment'],
        lang: 'en',
        dir: 'ltr',
        orientation: 'portrait-primary',
    }
}
