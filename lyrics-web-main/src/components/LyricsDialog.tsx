"use client";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { addHmacHeaders } from "@/lib/hmac";
import { cn } from "@/lib/utils";
import { Download, ThumbsDown, ThumbsUp, User } from "lucide-react";
import * as React from "react";
import { toast } from "sonner";

type Translation = {
  langCode: string;
  label: string;
  lyrics: string;
};

type TranslatedLyric = {
  id: string;
  videoId: string;
  translatedLyric: string;
  language: string;
  vote: number;
  contributor: string;
  contributorEmail: string;
};

type TranslatedLyricsResponse = {
  type: string;
  data: TranslatedLyric[];
  success: boolean;
};

type ContributorInfo = {
  name: string;
  email: string;
};

type VoteRequestDTO = {
  id: string;
  vote: number;
};

export type LyricsDialogData = {
  id: string;
  videoId: string;
  songTitle: string;
  artistName: string;
  albumName?: string;
  plainLyric?: string;
  syncedLyrics?: string;
  vote?: number;
  contributor?: string;
  contributorEmail?: string;
  translations?: Translation[];
};

type Props = {
  open: boolean;
  onOpenChange: (v: boolean) => void;
  data: LyricsDialogData | null;
};

function useTranslatedLyrics(videoId: string | null, isOpen: boolean) {
  const [state, setState] = React.useState<{
    loading: boolean;
    error: string | null;
    translatedLyrics: TranslatedLyric[];
  }>({ loading: false, error: null, translatedLyrics: [] });

  React.useEffect(() => {
    if (!videoId || !isOpen) {
      setState({ loading: false, error: null, translatedLyrics: [] });
      return;
    }

    const controller = new AbortController();
    async function fetchTranslations() {
      setState((s) => ({ ...s, loading: true, error: null }));
      try {
        const apiUrl = `https://api-lyrics.simpmusic.org/v1/translated/${videoId}`;
        const resp = await fetch(apiUrl, {
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

        const json: TranslatedLyricsResponse = await resp.json();
        if (json?.success === false) {
          throw new Error("Failed to fetch translated lyrics");
        }

        const translatedLyrics = Array.isArray(json?.data) ? json.data : [];
        setState({ loading: false, error: null, translatedLyrics });
      } catch (e: unknown) {
        if ((e as Error)?.name === "AbortError") return;
        setState({ loading: false, error: (e as Error)?.message ?? "Request failed", translatedLyrics: [] });
      }
    }

    fetchTranslations();
    return () => controller.abort();
  }, [videoId, isOpen]);

  return state;
}

// Cookie management functions
function setCookie(name: string, value: string, days: number = 30) {
  const expires = new Date();
  expires.setTime(expires.getTime() + days * 24 * 60 * 60 * 1000);
  document.cookie = `${name}=${value};expires=${expires.toUTCString()};path=/`;
}

function getCookie(name: string): string | null {
  const nameEQ = name + "=";
  const ca = document.cookie.split(';');
  for (let i = 0; i < ca.length; i++) {
    let c = ca[i];
    while (c.charAt(0) === ' ') c = c.substring(1, c.length);
    if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
  }
  return null;
}

function getStoredContributorInfo(): ContributorInfo | null {
  const name = getCookie('contributorName');
  const email = getCookie('contributorEmail');
  if (name && email) {
    return { name, email };
  }
  return null;
}

function storeContributorInfo(info: ContributorInfo) {
  setCookie('contributorName', info.name);
  setCookie('contributorEmail', info.email);
}

// Contributor Info Dialog Component
function ContributorDialog({
  open,
  onOpenChange,
  onSubmit
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (info: ContributorInfo) => void;
}) {
  const [name, setName] = React.useState('');
  const [email, setEmail] = React.useState('');
  const [isSubmitting, setIsSubmitting] = React.useState(false);

  React.useEffect(() => {
    if (open) {
      // Pre-fill with stored info if available
      const stored = getStoredContributorInfo();
      if (stored) {
        setName(stored.name);
        setEmail(stored.email);
      }
    }
  }, [open]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !email.trim()) return;

    setIsSubmitting(true);
    const info = { name: name.trim(), email: email.trim() };
    storeContributorInfo(info);
    onSubmit(info);
    setIsSubmitting(false);
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Contributor Information</DialogTitle>
          <DialogDescription>
            Please provide your information to vote on lyrics quality.
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="contributor-name">Name</Label>
            <Input
              id="contributor-name"
              type="text"
              placeholder="Your name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="contributor-email">Email</Label>
            <Input
              id="contributor-email"
              type="email"
              placeholder="your.email@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>
          <div className="flex justify-end gap-2">
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={!name.trim() || !email.trim() || isSubmitting}
            >
              {isSubmitting ? "Saving..." : "Save & Continue"}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}

export default function LyricsDialog({ open, onOpenChange, data }: Props) {
  const [activeLang, setActiveLang] = React.useState<string | null>(null);
  const [activeTranslatedId, setActiveTranslatedId] = React.useState<string | null>(null);
  const [contributorDialogOpen, setContributorDialogOpen] = React.useState(false);
  const [pendingVote, setPendingVote] = React.useState<{ itemId: string; vote: 1 | -1 } | null>(null);
  const [isVoting, setIsVoting] = React.useState(false);

  const hasTranslations = !!(data?.translations && data.translations.length > 0);
  const { loading: translatedLoading, translatedLyrics } = useTranslatedLyrics(data?.videoId || null, open);
  const [localTranslatedLyrics, setLocalTranslatedLyrics] = React.useState<TranslatedLyric[]>([]);
  const [localLyricsData, setLocalLyricsData] = React.useState<LyricsDialogData | null>(null);

  const hasTranslatedLyrics = localTranslatedLyrics.length > 0;

  // Sync with fetched data
  React.useEffect(() => {
    setLocalTranslatedLyrics(translatedLyrics);
  }, [translatedLyrics]);

  React.useEffect(() => {
    setLocalLyricsData(data);
  }, [data]);

  React.useEffect(() => {
    if (!open) {
      setActiveLang(null);
      setActiveTranslatedId(null);
      setContributorDialogOpen(false);
      setPendingVote(null);
      setIsVoting(false);
    } else if (hasTranslations) {
      // default to first translation if present
      setActiveLang((prev) => prev ?? data!.translations![0].langCode);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, hasTranslations]);

  const handleVote = async (itemId: string, vote: 1 | -1) => {
    const contributorInfo = getStoredContributorInfo();
    if (!contributorInfo) {
      // Show contributor dialog
      setPendingVote({ itemId, vote });
      setContributorDialogOpen(true);
      return;
    }

    await submitVote(itemId, vote, contributorInfo);
  };

  const handleContributorSubmit = async (contributorInfo: ContributorInfo) => {
    if (pendingVote) {
      await submitVote(pendingVote.itemId, pendingVote.vote, contributorInfo);
      setPendingVote(null);
    }
  };

  const submitVote = async (itemId: string, vote: 1 | -1, _contributorInfo: ContributorInfo) => {
    setIsVoting(true);
    try {
      // Determine if we're voting for translated lyrics or original lyrics
      const isTranslatedVote = !!activeTranslatedId;
      const requestUri = isTranslatedVote ? `/v1/translated/vote` : `/v1/vote`;
      const apiUrl = `https://api-lyrics.simpmusic.org${requestUri}`;

      const voteData: VoteRequestDTO = {
        id: itemId,
        vote: vote,
      };

      const requestBody = JSON.stringify(voteData);

      // Generate HMAC headers for authentication
      const baseHeaders = {
        "Content-Type": "application/json",
        Accept: "application/json",
      };

      const headersWithHmac = await addHmacHeaders(baseHeaders, requestUri);

      const resp = await fetch(apiUrl, {
        method: "POST",
        headers: headersWithHmac,
        mode: "cors",
        credentials: "omit",
        body: requestBody,
      });

      if (!resp.ok) {
        throw new Error(`HTTP ${resp.status}: ${resp.statusText}`);
      }

      // Parse response to get updated vote count
      const responseData = await resp.json();
      console.log(`Vote submitted successfully for ${isTranslatedVote ? 'translated' : 'original'} lyrics`, responseData);

      // Update local state with new vote count
      if (isTranslatedVote && responseData.success && responseData.data) {
        // Update translated lyrics vote count
        setLocalTranslatedLyrics(prev =>
          prev.map(lyric =>
            lyric.id === itemId
              ? { ...lyric, vote: responseData.data.vote }
              : lyric
          )
        );
        toast.success(`Vote submitted successfully for translation! New vote count: ${responseData.data.vote}`);
      } else if (!isTranslatedVote && responseData.success && responseData.data) {
        // Update original lyrics vote count
        setLocalLyricsData(prev =>
          prev ? { ...prev, vote: responseData.data.vote } : prev
        );
        toast.success(`Vote submitted successfully for lyrics! New vote count: ${responseData.data.vote}`);
      }
    } catch (error) {
      console.error("Failed to submit vote:", error);
      toast.error("Failed to submit vote. Please try again.");
    } finally {
      setIsVoting(false);
    }
  };

  const activeLyrics = React.useMemo(() => {
    if (!localLyricsData) return "";

    // Check if a translated lyric is selected
    if (activeTranslatedId) {
      const translatedLyric = localTranslatedLyrics.find((t) => t.id === activeTranslatedId);
      if (translatedLyric?.translatedLyric) return translatedLyric.translatedLyric;
    }

    // Check if a legacy translation is selected
    if (hasTranslations && activeLang) {
      const t = localLyricsData.translations!.find((x) => x.langCode === activeLang);
      if (t?.lyrics) return t.lyrics;
    }

    // Default to synced lyrics or plain lyrics
    return localLyricsData.syncedLyrics || localLyricsData.plainLyric || "";
  }, [localLyricsData, hasTranslations, activeLang, activeTranslatedId, localTranslatedLyrics]);

  const downloadLyrics = () => {
    if (!data || !activeLyrics) return;

    const content = data.syncedLyrics || data.plainLyric || "";
    const filename = `${data.songTitle} - ${data.artistName}.lrc`;

    const blob = new Blob([content], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-5xl max-h-[90vh] flex flex-col">
        <DialogHeader className="space-y-2 flex-shrink-0">
          <DialogTitle className="text-xl">
            {data ? `${data.songTitle} — ${data.artistName}` : "Lyrics"}
          </DialogTitle>
          {data?.albumName && (
            <p className="text-sm text-muted-foreground">Album: {data.albumName}</p>
          )}

          <div className="flex items-center gap-4 text-sm">
            {localLyricsData?.vote !== undefined && (
              <div className="flex items-center gap-1 text-muted-foreground">
                <ThumbsUp className="h-4 w-4" />
                <span>{localLyricsData.vote} votes</span>
              </div>
            )}
            {localLyricsData?.contributor && (
              <div className="flex items-center gap-1 text-muted-foreground">
                <User className="h-4 w-4" />
                <span>Contributed by {localLyricsData.contributor}</span>
              </div>
            )}
            {localLyricsData?.syncedLyrics && (
              <Badge variant="secondary" className="text-xs">
                Synced
              </Badge>
            )}
          </div>

          <DialogDescription className="pb-2">
            {hasTranslations ? "Select a translation or view original lyrics." : (
              hasTranslatedLyrics ? (
                <button
                  onClick={() => {
                    setActiveTranslatedId(null);
                    setActiveLang(null);
                  }}
                  className="text-muted-foreground hover:text-foreground transition-colors cursor-pointer underline-offset-4 hover:underline"
                >
                  Original lyrics
                </button>
              ) : "Original lyrics"
            )}
          </DialogDescription>

          {/* YouTube Embed */}
          {data?.videoId && (
            <div className="w-full aspect-video rounded-xl overflow-hidden mb-4 bg-black/50 shadow-2xl ring-1 ring-white/10 group relative">
              <iframe
                width="100%"
                height="100%"
                src={`https://www.youtube.com/embed/${data.videoId}?autoplay=0&rel=0&modestbranding=1`}
                title={data.songTitle}
                frameBorder="0"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                allowFullScreen
                className="absolute inset-0 z-10"
              />
            </div>
          )}
        </DialogHeader>

        {hasTranslations && (
          <div className="mb-3 flex flex-wrap gap-2 flex-shrink-0">
            {data!.translations!.map((t) => {
              const isActive = activeLang === t.langCode && !activeTranslatedId;
              return (
                <button
                  key={t.langCode}
                  type="button"
                  className={cn(
                    "inline-flex items-center rounded-full border px-3 py-1 text-xs font-medium transition-colors",
                    "hover:bg-accent hover:text-accent-foreground",
                    isActive
                      ? "bg-primary text-primary-foreground border-primary"
                      : "bg-background"
                  )}
                  onClick={() => {
                    setActiveLang(t.langCode);
                    setActiveTranslatedId(null);
                  }}
                >
                  {t.label}
                </button>
              );
            })}
            <Button
              variant="outline"
              size="sm"
              className="h-7 text-xs"
              onClick={() => {
                setActiveLang(null);
                setActiveTranslatedId(null);
              }}
            >
              Original
            </Button>
          </div>
        )}

        {(hasTranslatedLyrics || translatedLoading) && (
          <div className="mb-3 flex flex-wrap gap-2 flex-shrink-0">
            <div className="text-xs text-muted-foreground font-medium py-1">
              Community Translations:
            </div>
            {translatedLoading && (
              <div className="inline-flex items-center rounded-full border px-3 py-1 text-xs text-muted-foreground">
                Loading...
              </div>
            )}
            {localTranslatedLyrics.map((translation) => {
              const isActive = activeTranslatedId === translation.id;
              const languageLabel = translation.language.toUpperCase();
              return (
                <button
                  key={translation.id}
                  type="button"
                  className={cn(
                    "inline-flex items-center rounded-full border px-3 py-1 text-xs font-medium transition-colors",
                    "hover:bg-accent hover:text-accent-foreground",
                    isActive
                      ? "bg-primary text-primary-foreground border-primary"
                      : "bg-background"
                  )}
                  onClick={() => {
                    setActiveTranslatedId(translation.id);
                    setActiveLang(null);
                  }}
                >
                  {languageLabel}
                  {translation.vote > 0 && (
                    <span className="ml-1 text-xs opacity-70">
                      +{translation.vote}
                    </span>
                  )}
                </button>
              );
            })}
          </div>
        )}

        <div className="flex-1 min-h-0 flex flex-col">
          <pre className="flex-1 overflow-auto whitespace-pre-wrap rounded-lg border bg-muted/30 p-6 text-sm leading-relaxed font-mono">
            {activeLyrics || "No lyrics available."}
          </pre>
        </div>

        <div className="flex justify-between items-center pt-4 flex-shrink-0 border-t">
          <div className="flex items-center gap-4">
            {/* Vote buttons - show for translated lyrics or main lyrics */}
            {(activeTranslatedId || localLyricsData?.id) && (
              <div className="flex items-center gap-2">
                <span className="text-xs text-muted-foreground hidden sm:inline">
                  Rate this {activeTranslatedId ? "translation" : "lyrics"}:
                </span>
                <Button
                  onClick={() => {
                    const itemId = activeTranslatedId || localLyricsData!.id;
                    handleVote(itemId, 1);
                  }}
                  disabled={isVoting}
                  variant="outline"
                  size="sm"
                  className="gap-1 h-7 px-2"
                >
                  <ThumbsUp className="h-3 w-3" />
                  <span className="hidden sm:inline">
                    {isVoting ? "..." : "Good"}
                  </span>
                </Button>
                <Button
                  onClick={() => {
                    const itemId = activeTranslatedId || localLyricsData!.id;
                    handleVote(itemId, -1);
                  }}
                  disabled={isVoting}
                  variant="outline"
                  size="sm"
                  className="gap-1 h-7 px-2"
                >
                  <ThumbsDown className="h-3 w-3" />
                  <span className="hidden sm:inline">
                    {isVoting ? "..." : "Poor"}
                  </span>
                </Button>
              </div>
            )}
          </div>

          <Button
            onClick={downloadLyrics}
            disabled={!activeLyrics}
            variant="outline"
            size="sm"
            className="gap-2"
          >
            <Download className="h-4 w-4" />
            Download LRC
          </Button>
        </div>

        {/* Contributor Dialog */}
        <ContributorDialog
          open={contributorDialogOpen}
          onOpenChange={setContributorDialogOpen}
          onSubmit={handleContributorSubmit}
        />
      </DialogContent>
    </Dialog>
  );
}