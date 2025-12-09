export interface SearchResult {
    id: string;
    title: string;
    artist: string;
    thumbnail: string;
    duration: number;
}

const INSTANCES = [
    "https://inv.tux.pizza",
    "https://vid.puffyan.us",
    "https://invidious.projectsegfault.net",
    "https://invidious.drgns.space",
    "https://yt.artemislena.eu"
];

export async function searchYoutube(query: string): Promise<SearchResult[]> {
    for (const instance of INSTANCES) {
        try {
            const res = await fetch(`${instance}/api/v1/search?q=${encodeURIComponent(query)}&type=video`, {
                signal: AbortSignal.timeout(5000) // 5s timeout per instance
            });
            
            if (!res.ok) continue;
            
            const data = await res.json();
            
            // Map Invidious format to our Song format
            return data.map((item: any) => ({
                id: item.videoId,
                title: item.title,
                artist: item.author,
                thumbnail: item.videoThumbnails?.find((t: any) => t.quality === 'medium')?.url || `https://i.ytimg.com/vi/${item.videoId}/mqdefault.jpg`,
                duration: item.lengthSeconds
            }));
            
        } catch (e) {
            console.warn(`Instance ${instance} failed`, e);
            continue;
        }
    }
    throw new Error("All instances failed");
}
