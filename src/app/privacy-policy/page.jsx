import Link from 'next/link';
import React from 'react';

export const metadata = {
  title: "Privacy Policy | VxMusic - Free Music Streaming App",
  description: "Privacy Policy for VxMusic application. Learn how we collect, use, and protect your personal information.",
  keywords: "vxmusic privacy policy, data protection, personal information, music app privacy"
};

export default function PrivacyPolicy() {
  return (
    <div className="container mx-auto px-4 py-8 lg:px-16 md:px-16 sm:px-10">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-4xl font-bold mb-4 bg-clip-text text-transparent bg-gradient-to-r from-gradientstart/60 to-50% to-gradientend/60">
          Privacy Policy
        </h1>
        <p className="text-gray-600 dark:text-gray-400 mb-8">Effective Date: 01-04-2025</p>

        <div className="prose prose-lg dark:prose-invert max-w-none prose-headings:text-xl prose-headings:font-semibold prose-headings:my-6 prose-p:my-4 prose-a:text-blue-500 prose-a:no-underline hover:prose-a:underline prose-ul:my-4 prose-li:my-2">
          
          <p>
            <strong>Vishesh Gangwar</strong> built the VxMusic app as an Open Source app. This SERVICE is provided by Vishesh Gangwar at no cost and is intended for use as is.
          </p>

          <p>
            This page is used to inform visitors regarding my policies with the collection, use, and disclosure of Personal Information if anyone decided to use my Service. If you choose to use my Service, then you agree to the collection and use of information in relation to this policy.
          </p>

          <p>
            The Personal Information that I collect is used for providing and improving the Service. I will not use or share your information with anyone except as described in this Privacy Policy. The terms used in this Privacy Policy have the same meanings as in our <Link href="/terms" className="text-blue-500 hover:underline">Terms of Service</Link>, unless otherwise defined in this Privacy Policy.
          </p>

          <h2>Information Collection and Use</h2>
          <p>
            For a better experience, while using our Service, I may require you to provide us with certain personally identifiable information. The information that I request will be retained on your device and is not collected by me in any way.
          </p>
          
          <p>
            The app does use third-party services that may collect information used to identify you. Link to the privacy policy of third-party service providers used by the app <a href="https://policies.google.com/privacy" target="_blank" rel="noopener noreferrer">Google Play Services</a>.
          </p>

          <h2>Log Data</h2>
          <p>
            Log Data may include information such as your device Internet Protocol (&quot;IP&quot;) address, device name, operating system version, the configuration of the app when utilizing my Service, the time and date of your use of the Service, and other statistics.
          </p>
          
          <p>
            <strong>But none of this info leaves your device.</strong> It&apos;s stored in a log-file on your device only accessible to you.
          </p>

          <h2>Cookies</h2>
          <p>
            Cookies are files with a small amount of data that are commonly used as anonymous unique identifiers. These are sent to your browser from the websites that you visit and are stored on your device&apos;s internal memory.
          </p>
          
          <p>
            This Service does not use these &quot;cookies&quot; explicitly. However, some services may use cookies for their cause (e.g. improvement) but you can deny them once asked.
          </p>

            <h2>Advertising, Cookies, and Third-Party Vendors</h2>
            <p>
              This website uses Google AdSense and other third-party vendors to serve ads. These vendors, including Google, use cookies and similar technologies to serve ads based on your prior visits to this website or other websites on the Internet.
            </p>
            <p>
              <strong>Third-party vendors, including Google, may use cookies to serve ads based on your visits to this and/or other sites.</strong> Google&apos;s use of advertising cookies enables it and its partners to serve ads to you based on your visit to this site and/or other sites on the Internet.
            </p>
            <p>
              You may opt out of personalized advertising by visiting <a href="https://www.google.com/settings/ads" target="_blank" rel="noopener noreferrer">Google Ads Settings</a> or by visiting <a href="https://www.aboutads.info/choices" target="_blank" rel="noopener noreferrer">www.aboutads.info</a> for other third-party vendors.
            </p>
            <p>
              For more information on how Google uses data when you use our partners’ sites or apps, please visit <a href="https://policies.google.com/technologies/partner-sites" target="_blank" rel="noopener noreferrer">How Google uses data when you use our partners’ sites or apps</a>.
            </p>
            <p>
              <strong>We do not knowingly target or collect data from users under the age of 13 for personalized advertising.</strong>
            </p>

          <h2>Service Providers</h2>
          <p>I may employ third-party companies and individuals due to the following reasons:</p>
          <ul>
            <li>To facilitate our Service;</li>
            <li>To provide the Service on our behalf;</li>
            <li>To perform Service-related services; or</li>
            <li>To assist us in analyzing how our Service is used.</li>
          </ul>
          
          <p>
            I want to inform users of this Service that these third parties have access to their Personal Information. The reason is to perform the tasks assigned to them on our behalf. However, they are obligated not to disclose or use the information for any other purpose.
          </p>

          <h2>Security</h2>
          <p>
            I value your trust in providing us your Personal Information, thus we are striving to use commercially acceptable means of protecting it. But remember that no method of transmission over the internet, or method of electronic storage is 100% secure and reliable, and I cannot guarantee its absolute security.
          </p>

          <h2>Links to Other Sites</h2>
          <p>
            This Service may contain links to other sites. If you click on a third-party link, you will be directed to that site. Note that these external sites are not operated by me. Therefore, I strongly advise you to review the Privacy Policy of these websites. I have no control over and assume no responsibility for the content, privacy policies, or practices of any third-party sites or services.
          </p>

          <h2>Children&apos;s Privacy</h2>
          <p>
            These Services do not address anyone under the age of 13. I do not knowingly collect personally identifiable information from children under 13 years of age. In the case I discover that a child under 13 has provided me with personal information, I immediately delete this from our servers.
          </p>
          
          <p>
            If you are a parent or guardian and you are aware that your child has provided us with personal information, please contact me so that I will be able to do the necessary actions.
          </p>

          <h2>Changes to This Privacy Policy</h2>
          <p>
            I may update our Privacy Policy from time to time. Thus, you are advised to review this page periodically for any changes. I will notify you of any changes by posting the new Privacy Policy on this page.
          </p>
          
          <p><strong>This policy is effective as of 01-04-2025</strong></p>

          <h2>Contact Us</h2>
          <p>
            If you have any questions or suggestions about my Privacy Policy, do not hesitate to contact me at <a href="mailto:opabcg@gmail.com">opabcg@gmail.com</a>.
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