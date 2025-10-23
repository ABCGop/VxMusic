import Link from 'next/link';
import React from 'react';

export const metadata = {
  title: "Terms of Service | VxMusic - Free Music Streaming App",
  description: "Terms of Service and conditions for using VxMusic application.",
  keywords: "vxmusic terms of service, terms and conditions, user agreement"
};

export default function TermsOfService() {
  return (
    <div className="container mx-auto px-4 py-8 lg:px-16 md:px-16 sm:px-10">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-4xl font-bold mb-4 bg-clip-text text-transparent bg-gradient-to-r from-gradientstart/60 to-50% to-gradientend/60">
          Terms of Service
        </h1>
        <p className="text-gray-600 dark:text-gray-400 mb-8">Effective Date: 01-04-2025</p>

        <div className="prose prose-lg dark:prose-invert max-w-none prose-headings:text-xl prose-headings:font-semibold prose-headings:my-6 prose-p:my-4 prose-a:text-blue-500 prose-a:no-underline hover:prose-a:underline prose-ul:my-4 prose-li:my-2">
          
          <p>
            Welcome to VxMusic. These Terms of Service (&quot;Terms&quot;) govern your use of the VxMusic application and website operated by Vishesh Gangwar (&quot;us,&quot; &quot;we,&quot; or &quot;our&quot;).
          </p>

          <p>
            By accessing or using our Service, you agree to be bound by these Terms. If you disagree with any part of these terms, then you may not access the Service.
          </p>

          <h2>1. Description of Service</h2>
          <p>
            VxMusic is a free, open-source music streaming application that provides users with access to music content through a clean and user-friendly interface. The app is provided &quot;as is&quot; without any warranties.
          </p>

          <h2>2. User Responsibilities</h2>
          <p>You are responsible for:</p>
          <ul>
            <li>Ensuring your use of the app complies with all applicable laws and regulations</li>
            <li>Respecting intellectual property rights of content creators</li>
            <li>Not using the app for any illegal or unauthorized purpose</li>
            <li>Not attempting to reverse engineer, modify, or distribute the app without permission</li>
          </ul>

          <h2>3. Intellectual Property</h2>
          <p>
            VxMusic is open-source software. The app itself is provided under an open-source license, but this does not extend to any music content accessed through the app. All music content remains the property of their respective copyright holders.
          </p>

          <h2>4. Content and Copyright</h2>
          <p>
            VxMusic does not host, store, or distribute any copyrighted music content. The app serves as an interface for accessing publicly available content. Users are responsible for ensuring their use of any content complies with applicable copyright laws.
          </p>

          <h2>5. Disclaimers</h2>
          <p>
            The Service is provided on an &quot;AS IS&quot; and &quot;AS AVAILABLE&quot; basis. We make no warranties, expressed or implied, and hereby disclaim all other warranties including, without limitation, implied warranties or conditions of merchantability, fitness for a particular purpose, or non-infringement of intellectual property or other violation of rights.
          </p>

          <h2>6. Limitation of Liability</h2>
          <p>
            In no event shall VxMusic or its developers be liable for any indirect, incidental, special, consequential, or punitive damages, including without limitation, loss of profits, data, use, goodwill, or other intangible losses.
          </p>

          <h2>7. Third-Party Services</h2>
          <p>
            Our app may contain links to third-party websites or services that are not owned or controlled by VxMusic. We have no control over, and assume no responsibility for, the content, privacy policies, or practices of any third-party websites or services.
          </p>

          <h2>8. Modifications to Terms</h2>
          <p>
            We reserve the right to modify or replace these Terms at any time. If a revision is material, we will try to provide at least 30 days notice prior to any new terms taking effect.
          </p>

          <h2>9. Termination</h2>
          <p>
            We may terminate or suspend your access immediately, without prior notice or liability, for any reason whatsoever, including without limitation if you breach the Terms.
          </p>

          <h2>10. Governing Law</h2>
          <p>
            These Terms shall be interpreted and governed by the laws of India, without regard to its conflict of law provisions.
          </p>

          <h2>11. Contact Information</h2>
          <p>
            If you have any questions about these Terms of Service, please contact us at <a href="mailto:opabcg@gmail.com">opabcg@gmail.com</a>.
          </p>

        </div>

        <div className="mt-8">
          <Link href="/" className="text-blue-500 hover:underline inline-flex items-center">
            <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            Back to Home
          </Link>
        </div>
      </div>
    </div>
  );
}