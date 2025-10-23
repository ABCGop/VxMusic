import { ArrowDownTrayIcon, ArrowLeftIcon, CheckCircleIcon, CpuChipIcon, ShieldCheckIcon } from "@heroicons/react/24/outline";
import Link from "next/link";

export const metadata = {
  title: "Download VxMusic for Windows | Free Music Streaming App",
  description: "Download VxMusic for Windows PC. Free music streaming application with unlimited songs, offline downloads, and high-quality audio.",
};

export default function WindowsDownloadPage() {
  const downloadUrl = "https://github.com/ABCGop/VxMusic/releases/download/1.0/vxmusic-1.0.0.exe";
  const version = "1.0.0";

  const features = [
    {
      icon: <CheckCircleIcon className="w-6 h-6" />,
      title: "100% Free",
      description: "No subscriptions, no hidden costs, completely free forever"
    },
    {
      icon: <ArrowDownTrayIcon className="w-6 h-6" />,
      title: "Offline Downloads",
      description: "Download your favorite songs for offline listening"
    },
    {
      icon: <ShieldCheckIcon className="w-6 h-6" />,
      title: "No Ads",
      description: "Enjoy uninterrupted music streaming without any ads"
    },
    {
      icon: <CpuChipIcon className="w-6 h-6" />,
      title: "Lightweight",
      description: "Optimized for Windows with minimal system requirements"
    }
  ];

  const systemRequirements = [
    { label: "Operating System", value: "Windows 10 or later (64-bit)" },
    { label: "Processor", value: "Intel Core i3 or equivalent" },
    { label: "RAM", value: "4 GB minimum (8 GB recommended)" },
    { label: "Storage", value: "200 MB available space" },
    { label: "Internet", value: "Required for streaming" }
  ];

  const installSteps = [
    {
      number: "1",
      title: "Download",
      description: "Click the download button above to get the VxMusic installer"
    },
    {
      number: "2",
      title: "Run Installer",
      description: "Double-click the downloaded .exe file to start installation"
    },
    {
      number: "3",
      title: "Install",
      description: "Follow the installation wizard and accept the license agreement"
    },
    {
      number: "4",
      title: "Launch",
      description: "Open VxMusic from your desktop or Start menu and start listening"
    }
  ];

  return (
    <div className="min-h-screen bg-background">
      {/* Hero Section */}
      <section className="relative overflow-hidden bg-gradient-to-br from-primary/10 via-background to-accent/10 py-20">
        <div className="container mx-auto px-6">
          {/* Back Navigation */}
          <div className="mb-6">
            <Link 
              href="/download"
              className="inline-flex items-center gap-2 text-muted-foreground hover:text-foreground transition-colors"
            >
              <ArrowLeftIcon className="w-4 h-4" />
              Back to Downloads
            </Link>
          </div>

          <div className="max-w-4xl mx-auto text-center">
            <div className="mb-4">
              <span className="inline-block px-4 py-2 bg-primary/10 text-primary rounded-full text-sm font-medium">
                Windows Version
              </span>
            </div>
            <h1 className="text-4xl lg:text-6xl font-bold mb-6">
              Download VxMusic for <span className="text-primary">Windows</span>
            </h1>
            <p className="text-lg lg:text-xl text-muted-foreground mb-8 max-w-2xl mx-auto">
              Experience unlimited free music streaming on your Windows PC. High-quality audio, offline downloads, and zero ads.
            </p>

            {/* Download Button */}
            <div className="flex flex-col sm:flex-row gap-4 justify-center items-center mb-6">
              <a
                href={downloadUrl}
                className="btn-primary flex items-center gap-2 text-lg px-8 py-4"
                download
              >
                <ArrowDownTrayIcon className="w-6 h-6" />
                Download for Windows
              </a>
            </div>
            <p className="text-sm text-muted-foreground">
              Version {version} • Free • 20 MB
            </p>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 bg-background">
        <div className="container mx-auto px-6">
          <div className="text-center mb-12">
            <h2 className="text-3xl lg:text-4xl font-bold mb-4">
              Why Choose VxMusic for Windows?
            </h2>
            <p className="text-muted-foreground max-w-2xl mx-auto">
              Powerful music streaming features designed for the best desktop experience
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 max-w-6xl mx-auto">
            {features.map((feature, index) => (
              <div key={index} className="card-elevated p-6 text-center">
                <div className="inline-flex items-center justify-center w-12 h-12 rounded-full bg-primary/10 text-primary mb-4">
                  {feature.icon}
                </div>
                <h3 className="text-lg font-semibold mb-2">{feature.title}</h3>
                <p className="text-sm text-muted-foreground">{feature.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Installation Steps */}
      <section className="py-20 bg-muted/30">
        <div className="container mx-auto px-6">
          <div className="text-center mb-12">
            <h2 className="text-3xl lg:text-4xl font-bold mb-4">
              Easy Installation in 4 Steps
            </h2>
            <p className="text-muted-foreground max-w-2xl mx-auto">
              Get started with VxMusic on Windows in just a few minutes
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 max-w-6xl mx-auto">
            {installSteps.map((step, index) => (
              <div key={index} className="relative">
                <div className="card-elevated p-6">
                  <div className="inline-flex items-center justify-center w-12 h-12 rounded-full bg-primary text-primary-foreground text-xl font-bold mb-4">
                    {step.number}
                  </div>
                  <h3 className="text-lg font-semibold mb-2">{step.title}</h3>
                  <p className="text-sm text-muted-foreground">{step.description}</p>
                </div>
                {index < installSteps.length - 1 && (
                  <div className="hidden lg:block absolute top-1/2 -right-3 w-6 h-0.5 bg-border" />
                )}
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* System Requirements */}
      <section className="py-20 bg-background">
        <div className="container mx-auto px-6">
          <div className="max-w-3xl mx-auto">
            <div className="text-center mb-12">
              <h2 className="text-3xl lg:text-4xl font-bold mb-4">
                System Requirements
              </h2>
              <p className="text-muted-foreground">
                Make sure your PC meets these minimum requirements
              </p>
            </div>

            <div className="card-elevated p-8">
              <div className="space-y-4">
                {systemRequirements.map((req, index) => (
                  <div key={index} className="flex justify-between items-center py-3 border-b border-border last:border-b-0">
                    <span className="font-medium text-muted-foreground">{req.label}</span>
                    <span className="text-foreground font-semibold">{req.value}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* FAQ Section */}
      <section className="py-20 bg-muted/30">
        <div className="container mx-auto px-6">
          <div className="max-w-3xl mx-auto">
            <div className="text-center mb-12">
              <h2 className="text-3xl lg:text-4xl font-bold mb-4">
                Frequently Asked Questions
              </h2>
            </div>

            <div className="space-y-6">
              <div className="card-elevated p-6">
                <h3 className="text-lg font-semibold mb-2">Is VxMusic really free?</h3>
                <p className="text-muted-foreground">
                  Yes! VxMusic is completely free with no hidden costs, subscriptions, or premium tiers. All features are available to everyone.
                </p>
              </div>

              <div className="card-elevated p-6">
                <h3 className="text-lg font-semibold mb-2">Is it safe to download?</h3>
                <p className="text-muted-foreground">
                  Absolutely. VxMusic is open-source software hosted on GitHub. The installer is digitally signed and safe to use.
                </p>
              </div>

              <div className="card-elevated p-6">
                <h3 className="text-lg font-semibold mb-2">Do I need an account?</h3>
                <p className="text-muted-foreground">
                  No account is required to use VxMusic. Simply download, install, and start listening immediately.
                </p>
              </div>

              <div className="card-elevated p-6">
                <h3 className="text-lg font-semibold mb-2">Can I use it offline?</h3>
                <p className="text-muted-foreground">
                  Yes! VxMusic allows you to download songs for offline listening. You&apos;ll need an internet connection for streaming and downloading content.
                </p>
              </div>

              <div className="card-elevated p-6">
                <h3 className="text-lg font-semibold mb-2">How do I update VxMusic?</h3>
                <p className="text-muted-foreground">
                  VxMusic will notify you when updates are available. You can also check for updates in the app settings or download the latest version from this page.
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 bg-background">
        <div className="container mx-auto px-6">
          <div className="max-w-3xl mx-auto text-center">
            <h2 className="text-3xl lg:text-4xl font-bold mb-6">
              Ready to Experience Free Music Streaming?
            </h2>
            <p className="text-lg text-muted-foreground mb-8">
              Join thousands of users enjoying unlimited music on Windows
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <a
                href={downloadUrl}
                className="btn-primary flex items-center justify-center gap-2 text-lg px-8 py-4"
                download
              >
                <ArrowDownTrayIcon className="w-6 h-6" />
                Download Now
              </a>
              <Link href="/blog" className="btn-secondary text-lg px-8 py-4">
                Learn More
              </Link>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
