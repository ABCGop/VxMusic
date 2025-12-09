"use client";

import LyricsDialog, { type LyricsDialogData } from "@/components/LyricsDialog";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { useRouter, useSearchParams } from "next/navigation";
import React, { Suspense } from "react";

// Ensure types exist for API parsing
/* duplicate types removed */

interface ApiItem {
  id: string;
  videoId: string;
  songTitle: string;
  artistName: string;
  albumName?: string;
  durationSeconds?: number;
  plainLyric?: string;
  syncedLyrics?: string;
  richSyncLyrics?: string;
  vote?: number;
  contributor?: string;
  contributorEmail?: string;
}

interface ApiResponse {
  data?: ApiItem[];
  success?: boolean;
  error?: {
    error: boolean;
    code: number;
    reason: string;
  };
}

function useSearchResults(q: string) {
  const [state, setState] = React.useState<{
    loading: boolean;
    error: string | null;
    items: ApiItem[];
    requestTime: number | null;
  }>({ loading: false, error: null, items: [], requestTime: null });

  React.useEffect(() => {
    const query = q.trim();
    if (!query) {
      setState({ loading: false, error: null, items: [], requestTime: null });
      return;
    }

    const controller = new AbortController();
    async function run() {
      const startTime = Date.now();
      setState((s) => ({ ...s, loading: true, error: null, requestTime: null }));
      try {
        // Always call the external API. Do NOT hit local /search page route.
        const apiUrl = new URL("https://api-lyrics.simpmusic.org/v1/search");
        apiUrl.searchParams.set("q", query);
        apiUrl.searchParams.set("limit", "10");
        apiUrl.searchParams.set("offset", "0");

        const resp = await fetch(apiUrl.toString(), {
          method: "GET",
          signal: controller.signal,
          headers: {
            Accept: "application/json",
          },
          mode: "cors",
          cache: "no-store",
          credentials: "omit",
        });

        if (!resp.ok) {
          throw new Error(`HTTP ${resp.status}`);
        }

        const json: ApiResponse = await resp.json();
        if (json?.success === false || json?.error?.error) {
          throw new Error(json?.error?.reason ?? "Unknown API error");
        }

        let items = Array.isArray(json?.data) ? json.data : [];

        // Filter out non-music content
        items = items.filter((item) => {
          const lowerTitle = item.songTitle.toLowerCase();
          const lowerArtist = item.artistName.toLowerCase();
          const banned = ["gameplay", "walkthrough", "tutorial", "review", "reaction", "trailer", "teaser", "unboxing"];
          return !banned.some(b => lowerTitle.includes(b) || lowerArtist.includes(b));
        });

        const endTime = Date.now();
        const requestTime = endTime - startTime;
        setState({ loading: false, error: null, items, requestTime });
      } catch (e: unknown) {
        if ((e as Error)?.name === "AbortError") return;
        setState({ loading: false, error: (e as Error)?.message ?? "Request failed", items: [], requestTime: null });
      }
    }

    run();
    return () => controller.abort();
  }, [q]);

  return state;
}

function SearchResultsWithDialog({ items }: { items: ApiItem[] }) {
  const [open, setOpen] = React.useState(false);
  const [data, setData] = React.useState<LyricsDialogData | null>(null);

  function openDialogFromItem(it: ApiItem) {
    // Map API item to dialog data
    const translations = parseTranslationsFromItem(it);
    const d: LyricsDialogData = {
      id: it.id,
      videoId: it.videoId,
      songTitle: it.songTitle,
      artistName: it.artistName,
      albumName: it.albumName,
      plainLyric: (it.plainLyric ?? "").trim(),
      syncedLyrics: (it.syncedLyrics ?? "").trim(),
      vote: it.vote,
      contributor: it.contributor,
      contributorEmail: it.contributorEmail,
      translations,
    };
    setData(d);
    setOpen(true);
  }

  return (
    <div className="space-y-3">
      {items.map((it) => {
        const hasSynced = !!(it.syncedLyrics && it.syncedLyrics.trim().length > 0);
        return (
          <Card
            key={it.id}
            className="hover:bg-accent/30 transition-colors cursor-pointer"
            onClick={() => openDialogFromItem(it)}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => {
              if (e.key === "Enter" || e.key === " ") {
                e.preventDefault();
                openDialogFromItem(it);
              }
            }}
            aria-label={`Open lyrics for ${it.songTitle} by ${it.artistName}`}
          >
            <CardContent className="py-4">
              <div className="flex items-start justify-between gap-4">
                <div className="min-w-0">
                  <div className="flex items-center gap-2">
                    <h3 className="text-base font-semibold truncate">
                      {it.songTitle}
                    </h3>
                    <SyncedBadge has={hasSynced} />
                  </div>
                  <p className="text-sm text-muted-foreground truncate">
                    {it.artistName}{it.albumName ? ` • ${it.albumName}` : ""}
                  </p>
                </div>
                <Duration seconds={it.durationSeconds} />
              </div>
            </CardContent>
          </Card>
        );
      })}

      <LyricsDialog open={open} onOpenChange={setOpen} data={data} />
    </div>
  );
}

function parseTranslationsFromItem(it: ApiItem): LyricsDialogData["translations"] {
  // Heuristic: if API later returns translated lyrics fields, adapt here.
  // For now, attempt to parse from richSyncLyrics or syncedLyrics if they embed translations.
  // Fallback: none.
  const translations: LyricsDialogData["translations"] = [];

  // Example stub: if richSyncLyrics contains markers like [lang:xx] ... [/lang]
  const rich = it.richSyncLyrics ?? "";
  const blocks = Array.from(rich.matchAll(/\[lang:([a-z-]+)\]([\s\S]*?)\[\/lang\]/gi));
  for (const m of blocks) {
    const code = (m[1] ?? "").toLowerCase();
    const text = (m[2] ?? "").trim();
    if (code && text) {
      translations.push({
        langCode: code,
        label: code.toUpperCase(),
        lyrics: text,
      });
    }
  }

  return translations.length > 0 ? translations : undefined;
}

function SearchBar() {
  const router = useRouter();
  const params = useSearchParams();
  const q = params.get("q") ?? "";
  const [term, setTerm] = React.useState(q);

  React.useEffect(() => {
    // keep state in sync when url changes (e.g., back/forward)
    setTerm(q);

  }, [q]);

  function submit() {
    const next = term.trim();
    const url = next ? `/search?q=${encodeURIComponent(next)}` : `/search`;
    router.push(url);
  }

  function onKeyDown(e: React.KeyboardEvent<HTMLInputElement>) {
    if (e.key === "Enter") {
      e.preventDefault();
      submit();
    }
  }

  return (
    <div className="flex items-center gap-2 w-full">
      <Input
        type="text"
        placeholder="Search lyrics, artists, songs..."
        aria-label="Search lyrics"
        className="h-12 text-base"
        value={term}
        onChange={(e) => setTerm(e.target.value)}
        onKeyDown={onKeyDown}
      />
      <Button className="h-12 px-6 text-base" onClick={submit} disabled={!term.trim()}>
        Search
      </Button>
    </div>
  );
}

function Duration({ seconds }: { seconds?: number }) {
  if (!seconds && seconds !== 0) return null;
  const m = Math.floor(seconds / 60);
  const s = (seconds % 60).toString().padStart(2, "0");
  return <span className="tabular-nums text-muted-foreground text-sm">{m}:{s}</span>;
}

function SyncedBadge({ has }: { has: boolean }) {
  if (!has) return null;
  return (
    <span className="inline-flex items-center rounded-md bg-emerald-500/10 px-2 py-0.5 text-xs font-medium text-emerald-600 ring-1 ring-inset ring-emerald-500/20">
      Synced
    </span>
  );
}

function SearchPageContent() {
  const params = useSearchParams();
  const q = params.get("q") ?? "";
  const hasQuery = q.trim().length > 0;

  const { loading, error, items, requestTime } = useSearchResults(q);

  return (
    <div className="min-h-[calc(100vh-64px)] px-4 pt-24 pb-10">
      <section className="mx-auto w-full max-w-4xl space-y-6">
        <header className="space-y-3 text-center">
          <h1 className="text-2xl md:text-3xl font-bold tracking-tight">Search</h1>
          <p className="text-muted-foreground">Find lyrics, artists, and songs</p>
        </header>

        <SearchBar />

        <div className="pt-4">
          {!hasQuery && (
            <Card>
              <CardHeader>
                <CardTitle className="text-lg">Start searching</CardTitle>
              </CardHeader>
              <CardContent className="text-muted-foreground">
                Type a song, artist, or lyric snippet above and press Enter.
              </CardContent>
            </Card>
          )}

          {hasQuery && (
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <h2 className="text-sm text-muted-foreground">
                  Results for: <span className="font-medium text-foreground">&quot;{q}&quot;</span>
                </h2>
                {requestTime && !loading && (
                  <span className="text-xs text-muted-foreground tabular-nums">
                    {requestTime}ms
                  </span>
                )}
              </div>

              {loading && (
                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg">Loading…</CardTitle>
                  </CardHeader>
                  <CardContent className="text-muted-foreground">
                    Fetching results from the lyrics API.
                  </CardContent>
                </Card>
              )}

              {error && !loading && (
                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg">Error</CardTitle>
                  </CardHeader>
                  <CardContent className="text-destructive">
                    {error}
                  </CardContent>
                </Card>
              )}

              {!loading && !error && items.length === 0 && (
                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg">No results</CardTitle>
                  </CardHeader>
                  <CardContent className="text-muted-foreground">
                    No matches were found for this query.
                  </CardContent>
                </Card>
              )}

              {!loading && !error && items.length > 0 && (
                <SearchResultsWithDialog items={items} />
              )}
            </div>
          )}
        </div>
      </section>
    </div>
  );
}

export default function SearchPage() {
  return (
    <Suspense fallback={
      <div className="min-h-[calc(100vh-64px)] px-4 pt-24 pb-10">
        <section className="mx-auto w-full max-w-4xl space-y-6">
          <header className="space-y-3 text-center">
            <h1 className="text-2xl md:text-3xl font-bold tracking-tight">Search</h1>
            <p className="text-muted-foreground">Find lyrics, artists, and songs</p>
          </header>
          <div className="h-12 bg-muted animate-pulse rounded"></div>
          <div className="space-y-3">
            <div className="h-20 bg-muted animate-pulse rounded"></div>
          </div>
        </section>
      </div>
    }>
      <SearchPageContent />
    </Suspense>
  );
}
