"use client"

import * as React from "react"
import { cn } from "@/lib/utils"

/**
  NOTE:
  - This Dialog implementation is headless and Tailwind-based to align with shadcn/ui usage without importing Radix directly.
  - API surface mirrors shadcn/ui Dialog components so it can be used like:
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader><DialogTitle>...</DialogTitle></DialogHeader>
          ...
        </DialogContent>
      </Dialog>

  - Controlled:
      <Dialog open={bool} onOpenChange={(v) => ...}>...</Dialog>
  - Uncontrolled:
      <Dialog defaultOpen>...</Dialog>

  - DialogTrigger simply calls context.onOpenChange(true) on click.
*/

type DialogContextValue = {
  open: boolean
  setOpen: (v: boolean) => void
}

const DialogContext = React.createContext<DialogContextValue | null>(null)

function useDialogCtx() {
  const ctx = React.useContext(DialogContext)
  if (!ctx) {
    throw new Error("Dialog components must be used within <Dialog>")
  }
  return ctx
}

type DialogProps = {
  open?: boolean
  defaultOpen?: boolean
  onOpenChange?: (open: boolean) => void
  children?: React.ReactNode
}

function Dialog({ open, defaultOpen, onOpenChange, children }: DialogProps) {
  const isControlled = open !== undefined
  const [internalOpen, setInternalOpen] = React.useState(!!defaultOpen)
  const actualOpen = isControlled ? !!open : internalOpen

  const setOpen = React.useCallback(
    (v: boolean) => {
      if (!isControlled) setInternalOpen(v)
      onOpenChange?.(v)
    },
    [isControlled, onOpenChange]
  )

  const value = React.useMemo(() => ({ open: actualOpen, setOpen }), [actualOpen, setOpen])

  return <DialogContext.Provider value={value}>{children}</DialogContext.Provider>
}

type DialogTriggerProps = React.ComponentProps<"button"> & {
  asChild?: boolean
}

function DialogTrigger({ asChild, className, ...props }: DialogTriggerProps) {
  const { setOpen } = useDialogCtx()
  const Comp = asChild ? "span" : "button"
  return (
    <Comp
      data-slot="dialog-trigger"
      className={className}
      onClick={(e: React.MouseEvent) => {
        props.onClick?.(e as React.MouseEvent<HTMLButtonElement>)
        if (!e.defaultPrevented) setOpen(true)
      }}
      {...props}
    />
  )
}

type DialogPortalProps = {
  children?: React.ReactNode
  container?: HTMLElement
  className?: string
}

function DialogPortal({ children }: DialogPortalProps) {
  // For simplicity, render in place. Next/SSR-safe without portal.
  return <>{children}</>
}

type DialogOverlayProps = React.ComponentProps<"div">

function DialogOverlay({ className, ...props }: DialogOverlayProps) {
  const { open, setOpen } = useDialogCtx()
  if (!open) return null
  return (
    <div
      data-slot="dialog-overlay"
      className={cn(
        "fixed inset-0 z-50 bg-black/50 backdrop-blur-sm",
        "animate-in fade-in-0",
        className
      )}
      onClick={() => setOpen(false)}
      {...props}
    />
  )
}

type DialogContentProps = React.ComponentProps<"div">

function DialogContent({ className, children, ...props }: DialogContentProps) {
  const { open, setOpen } = useDialogCtx()
  if (!open) return null
  return (
    <DialogPortal>
      <div
        role="dialog"
        aria-modal="true"
        data-slot="dialog-content"
        className={cn(
          "fixed left-1/2 top-1/2 z-[60] w-[95vw] max-w-2xl -translate-x-1/2 -translate-y-1/2",
          "rounded-xl border bg-background p-6 shadow-lg outline-none",
          "animate-in fade-in-0 zoom-in-95 slide-in-from-top-2",
          className
        )}
        {...props}
      >
        {/* Close button */}
        <button
          type="button"
          aria-label="Close"
          className={cn(
            "absolute right-4 top-4 rounded-md opacity-70 transition-opacity",
            "hover:opacity-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring/50"
          )}
          onClick={() => setOpen(false)}
        >
          <span className="sr-only">Close</span>
          <svg viewBox="0 0 24 24" className="size-4" aria-hidden="true">
            <path d="M6 6l12 12M18 6L6 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
          </svg>
        </button>

        {children}
      </div>
    </DialogPortal>
  )
}

function DialogHeader({ className, ...props }: React.ComponentProps<"div">) {
  return (
    <div
      data-slot="dialog-header"
      className={cn("flex flex-col space-y-1.5 text-center sm:text-left", className)}
      {...props}
    />
  )
}

function DialogFooter({ className, ...props }: React.ComponentProps<"div">) {
  return (
    <div
      data-slot="dialog-footer"
      className={cn("flex flex-col-reverse sm:flex-row sm:justify-end sm:space-x-2", className)}
      {...props}
    />
  )
}

function DialogTitle({ className, ...props }: React.ComponentProps<"h2">) {
  return (
    <h2
      data-slot="dialog-title"
      className={cn("text-lg font-semibold leading-none tracking-tight", className)}
      {...props}
    />
  )
}

function DialogDescription({ className, ...props }: React.ComponentProps<"p">) {
  return (
    <p
      data-slot="dialog-description"
      className={cn("text-sm text-muted-foreground", className)}
      {...props}
    />
  )
}

export {
  Dialog,
  DialogPortal,
  DialogOverlay,
  DialogTrigger,
  DialogContent,
  DialogHeader,
  DialogFooter,
  DialogTitle,
  DialogDescription,
}