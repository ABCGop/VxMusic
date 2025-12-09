import Script from 'next/script'

interface StructuredDataProps {
    type: 'website' | 'about' | 'search'
    data?: any
}

export default function StructuredData({ type, data }: StructuredDataProps) {
    const getStructuredData = () => {
        const baseData = {
            "@context": "https://schema.org",
            "@type": "WebSite",
            "name": "SimpMusic Lyrics",
            "description": "Discover synced lyrics and translations for your favorite songs. Free lyrics database with YouTube Music integration.",
            "url": "https://lyrics.simpmusic.org",
            "logo": "https://lyrics.simpmusic.org/logo_simpmusic.png",
            "sameAs": [
                "https://github.com/maxrave-dev/lyrics",
                "https://twitter.com/maxrave_dev"
            ],
            "potentialAction": {
                "@type": "SearchAction",
                "target": {
                    "@type": "EntryPoint",
                    "urlTemplate": "https://lyrics.simpmusic.org/search?q={search_term_string}"
                },
                "query-input": "required name=search_term_string"
            }
        }

        switch (type) {
            case 'website':
                return baseData

            case 'about':
                return {
                    ...baseData,
                    "@type": "AboutPage",
                    "mainEntity": {
                        "@type": "Organization",
                        "name": "SimpMusic",
                        "description": "A FOSS YouTube Music client for Android with many features from Spotify, Musixmatch, SponsorBlock, ReturnYouTubeDislike.",
                        "url": "https://lyrics.simpmusic.org",
                        "logo": "https://lyrics.simpmusic.org/logo_simpmusic.png",
                        "founder": {
                            "@type": "Person",
                            "name": "Nguyễn Đức Tuấn Minh",
                            "url": "https://github.com/maxrave-dev",
                            "sameAs": [
                                "https://twitter.com/maxrave_dev",
                                "https://www.linkedin.com/in/maxrave"
                            ]
                        },
                        "foundingDate": "2023",
                        "applicationCategory": "MusicApplication",
                        "operatingSystem": "Android"
                    }
                }

            case 'search':
                return {
                    ...baseData,
                    "@type": "SearchResultsPage",
                    "mainEntity": {
                        "@type": "ItemList",
                        "numberOfItems": data?.numberOfItems || 0,
                        "itemListElement": data?.results?.map((item: any, index: number) => ({
                            "@type": "ListItem",
                            "position": index + 1,
                            "item": {
                                "@type": "MusicRecording",
                                "name": item.songTitle,
                                "byArtist": {
                                    "@type": "Person",
                                    "name": item.artistName
                                },
                                "inAlbum": item.albumName ? {
                                    "@type": "MusicAlbum",
                                    "name": item.albumName
                                } : undefined,
                                "duration": item.durationSeconds ? `PT${item.durationSeconds}S` : undefined,
                                "lyrics": {
                                    "@type": "CreativeWork",
                                    "text": item.plainLyric
                                }
                            }
                        })) || []
                    }
                }

            default:
                return baseData
        }
    }

    return (
        <Script
            id={`structured-data-${type}`}
            type="application/ld+json"
            dangerouslySetInnerHTML={{
                __html: JSON.stringify(getStructuredData())
            }}
        />
    )
}
