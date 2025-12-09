export default function PrivacyPage() {
  return (
    <div className="p-8 md:p-12 max-w-4xl mx-auto min-h-full">
      <h1 className="text-4xl font-bold mb-8">Privacy Policy</h1>
      
      <div className="space-y-8 text-gray-300">
        <section>
            <h2 className="text-2xl font-semibold mb-4 text-white">1. Introduction</h2>
            <p className="leading-relaxed">
                Welcome to VxMusic. We value your privacy and are committed to protecting your personal information.
                This Privacy Policy explains how we collect, use, and safeguard your data when you use our music streaming service.
            </p>
        </section>

        <section>
            <h2 className="text-2xl font-semibold mb-4 text-white">2. Information We Collect</h2>
            <ul className="list-disc pl-6 space-y-2">
                <li><strong className="text-white">Usage Data:</strong> We collect non-personal data regarding your interactions with the app, such as songs played, playlists created, and search queries.</li>
                <li><strong className="text-white">Device Information:</strong> We may collect information about the device you use to access VxMusic, including the hardware model and operating system.</li>
            </ul>
        </section>

        <section>
            <h2 className="text-2xl font-semibold mb-4 text-white">3. How We Use Your Data</h2>
            <p className="leading-relaxed">
                We use the collected data to:
            </p>
            <ul className="list-disc pl-6 space-y-2 mt-2">
                <li>Identify popular content and improve our recommendation algorithms.</li>
                <li>Maintain and improve the stability of our service.</li>
                <li>Provide customer support.</li>
            </ul>
        </section>

        <section>
            <h2 className="text-2xl font-semibold mb-4 text-white">4. Data Security</h2>
            <p className="leading-relaxed">
                We implement reasonable security measures to protect your information. However, please note that no method of transmission over the internet is 100% secure.
            </p>
        </section>

        <section>
            <h2 className="text-2xl font-semibold mb-4 text-white">5. Contact Us</h2>
            <p className="leading-relaxed">
                If you have any questions about this Privacy Policy, please contact us at support@vxmusic.in.
            </p>
        </section>
      </div>
    </div>
  );
}
