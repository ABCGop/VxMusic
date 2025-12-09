'use client';

export default function Terms() {
  return (
    <div className="flex-1 container mx-auto px-6 pt-32 pb-20 max-w-4xl">
      <div className="mb-12 border-b border-white/10 pb-8">
          <h1 className="text-4xl md:text-5xl font-bold mb-4 title-glow">Terms of Service</h1>
          <p className="text-gray-400">Effective Date: 01-04-2025</p>
      </div>
      
      <div className="space-y-12 text-gray-300 text-lg leading-relaxed">
        <section>
            <p>
                Welcome to VxMusic. These Terms of Service ("Terms") govern your use of the VxMusic application and website operated by Vishesh Gangwar ("us," "we," or "our").
            </p>
            <p className="mt-4">
                By accessing or using our Service, you agree to be bound by these Terms. If you disagree with any part of these terms, then you may not access the Service.
            </p>
        </section>

        <Section title="1. Description of Service">
            VxMusic is a free, open-source music streaming application that provides users with access to music content through a clean and user-friendly interface. The app is provided "as is" without any warranties.
        </Section>

        <Section title="2. User Responsibilities">
            <p className="mb-4">You are responsible for:</p>
            <ul className="list-disc pl-6 space-y-2 text-gray-400">
                <li>Ensuring your use of the app complies with all applicable laws and regulations</li>
                <li>Respecting intellectual property rights of content creators</li>
                <li>Not using the app for any illegal or unauthorized purpose</li>
                <li>Not attempting to reverse engineer, modify, or distribute the app without permission</li>
            </ul>
        </Section>

        <Section title="3. Intellectual Property">
            VxMusic is open-source software. The app itself is provided under an open-source license, but this does not extend to any music content accessed through the app. All music content remains the property of their respective copyright holders.
        </Section>

        <Section title="4. Content and Copyright">
            VxMusic does not host, store, or distribute any copyrighted music content. The app serves as an interface for accessing publicly available content. Users are responsible for ensuring their use of any content complies with applicable copyright laws.
        </Section>

        <Section title="5. Disclaimers">
            The Service is provided on an "AS IS" and "AS AVAILABLE" basis. We make no warranties, expressed or implied, and hereby disclaim all other warranties including, without limitation, implied warranties or conditions of merchantability, fitness for a particular purpose, or non-infringement of intellectual property or other violation of rights.
        </Section>

        <Section title="6. Limitation of Liability">
            In no event shall VxMusic or its developers be liable for any indirect, incidental, special, consequential, or punitive damages, including without limitation, loss of profits, data, use, goodwill, or other intangible losses.
        </Section>

        <Section title="7. Third-Party Services">
            Our app may contain links to third-party websites or services that are not owned or controlled by VxMusic. We have no control over, and assume no responsibility for, the content, privacy policies, or practices of any third-party websites or services.
        </Section>

        <Section title="8. Modifications to Terms">
            We reserve the right to modify or replace these Terms at any time. If a revision is material, we will try to provide at least 30 days notice prior to any new terms taking effect.
        </Section>

        <Section title="9. Termination">
            We may terminate or suspend your access immediately, without prior notice or liability, for any reason whatsoever, including without limitation if you breach the Terms.
        </Section>

        <Section title="10. Governing Law">
            These Terms shall be interpreted and governed by the laws of India, without regard to its conflict of law provisions.
        </Section>

        <Section title="11. Contact Information">
             If you have any questions about these Terms of Service, please contact us at <a href="mailto:opabcg@gmail.com" className="text-indigo-400 hover:text-indigo-300 transition">opabcg@gmail.com</a>.
        </Section>
      </div>
    </div>
  );
}

function Section({ title, children }: { title: string, children: React.ReactNode }) {
    return (
        <section className="p-6 rounded-2xl bg-white/5 border border-white/5 hover:border-white/10 transition duration-300">
            <h2 className="text-xl font-bold text-white mb-4">{title}</h2>
            <div className="text-gray-400">
                {children}
            </div>
        </section>
    )
}
