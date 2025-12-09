import Link from 'next/link'
import { Button } from '@/components/ui/button'

export default function NotFound() {
  return (
    <div className="min-h-screen flex items-center justify-center px-4">
      <div className="text-center space-y-6 max-w-md">
        <div className="space-y-2">
          <h1 className="text-6xl font-bold text-primary">404</h1>
          <h2 className="text-2xl font-semibold">Page Not Found</h2>
          <p className="text-muted-foreground">
            The page you are looking for does not exist or has been moved.
          </p>
        </div>
        
        <div className="space-y-4">
          <Button asChild className="w-full">
            <Link href="/">
              Return Home
            </Link>
          </Button>
          
          <Button asChild variant="outline" className="w-full">
            <Link href="/search">
              Search Lyrics
            </Link>
          </Button>
        </div>
      </div>
    </div>
  )
}
