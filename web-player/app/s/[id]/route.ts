import { NextRequest, NextResponse } from 'next/server';

export async function GET(
    request: NextRequest,
    { params }: { params: { id: string } }
) {
    const id = params.id;
    const userAgent = request.headers.get('user-agent') || '';
    const isAndroid = /Android/i.test(userAgent);

    // If Android, try to open app, fallback to web play
    if (isAndroid) {
        // We can't easily execute JS to fallback in a pure generic route on server side 
        // without serving an HTML page.
        // So we serve a simple HTML that tries the intent.
        const html = `
      <!DOCTYPE html>
      <html>
        <head>
          <title>Redirecting...</title>
          <meta property="og:title" content="VxMusic Song" />
          <meta property="og:description" content="Listen on VxMusic" />
        </head>
        <body>
          <p>Opening VxMusic...</p>
          <script>
            // Try to open App
            window.location.href = "vxmusic://play?id=${id}";
            
            // Fallback to web player after a timeout
            setTimeout(function() {
               window.location.href = "/play?id=${id}"; 
            }, 1000);
          </script>
        </body>
      </html>
    `;
        return new NextResponse(html, {
            headers: { 'Content-Type': 'text/html' },
        });
    }

    // If not Android (e.g. Desktop, iOS), go to web player
    return NextResponse.redirect(new URL(`/play?id=${id}`, request.url));
}
