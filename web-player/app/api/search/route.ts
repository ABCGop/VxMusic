import { NextRequest, NextResponse } from 'next/server';

export async function GET(request: NextRequest) {
  const searchParams = request.nextUrl.searchParams;
  const query = searchParams.get('q');

  if (!query) {
    return NextResponse.json({ error: 'Missing query' }, { status: 400 });
  }

  try {
    const res = await fetch(`https://www.youtube.com/results?search_query=${encodeURIComponent(query)}`, {
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
      }
    });
    
    const html = await res.text();
    
    // Extract ytInitialData
    const match = html.match(/var ytInitialData = ({.*?});/);
    if (!match) {
         throw new Error("Failed to parse YouTube data");
    }
    
    const data = JSON.parse(match[1]);
    const contents = data.contents.twoColumnSearchResultsRenderer.primaryContents.sectionListRenderer.contents[0].itemSectionRenderer.contents;
    
    const results = contents
        .filter((item: any) => item.videoRenderer)
        .map((item: any) => {
            const video = item.videoRenderer;
            return {
                id: video.videoId,
                title: video.title.runs[0].text,
                artist: video.ownerText?.runs[0]?.text || "Unknown",
                thumbnail: video.thumbnail.thumbnails[0].url,
                duration: parseDuration(video.lengthText?.simpleText)
            };
        });

    return NextResponse.json(results);
    
  } catch (error) {
    console.error("Scraper failed", error);
    return NextResponse.json({ error: 'Search failed' }, { status: 500 });
  }
}

function parseDuration(text: string): number {
    if(!text) return 0;
    const parts = text.split(':').map(Number);
    if (parts.length === 2) return parts[0] * 60 + parts[1];
    if (parts.length === 3) return parts[0] * 3600 + parts[1] * 60 + parts[2];
    return 0;
}
