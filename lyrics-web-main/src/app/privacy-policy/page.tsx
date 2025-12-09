import React from 'react';
import Link from 'next/link';
import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Privacy Policy",
  description: "Privacy Policy for SimpMusic Lyrics. Learn how we collect, use, and protect your personal information when you use our service.",
  openGraph: {
    title: "Privacy Policy | SimpMusic Lyrics",
    description: "Privacy Policy for SimpMusic Lyrics. Learn how we collect, use, and protect your personal information.",
    url: "https://lyrics.simpmusic.org/privacy-policy",
  },
  twitter: {
    title: "Privacy Policy | SimpMusic Lyrics",
    description: "Privacy Policy for SimpMusic Lyrics. Learn how we collect, use, and protect your personal information.",
  },
  alternates: {
    canonical: "https://lyrics.simpmusic.org/privacy-policy",
  },
};

export default function PrivacyPolicy() {
  return (
    <div className="container mx-auto px-4 py-8 lg:px-16 md:px-16 sm:px-10">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-4xl font-bold mb-4 bg-clip-text text-transparent bg-gradient-to-r from-gradientstart/60 to-50% to-gradientend/60">
          Privacy Policy for SimpMusic.org
        </h1>
        <p className="text-gray-600 dark:text-gray-400 mb-8">Effective Date: 12/6/2025</p>

        <div className="prose prose-lg dark:prose-invert max-w-none prose-headings:text-xl prose-headings:font-semibold prose-headings:my-4 prose-p:my-3 prose-a:text-blue-500 prose-a:no-underline hover:prose-a:underline">
          <p>Welcome to SimpMusic.org!</p>

          <p>At SimpMusic.org (hereinafter referred to as &quot;us,&quot; &quot;we,&quot; or &quot;our&quot;), we respect the privacy of our visitors and are committed to protecting your personal information. This Privacy Policy explains how we collect, use, disclose, and safeguard your information when you visit our website.</p>

          <p>Please read this policy carefully. By accessing or using our website, you agree to the terms outlined in this Privacy Policy.</p>

          <h2>1. Information We Collect</h2>
          <p>We may collect information about you in various ways.</p>

          <p><strong>a. Personal Data You Provide to Us:</strong></p>
          <ul>
            <li><strong>Contact Information:</strong> When you contact us via a contact form or email, we may collect your name, email address, and any other information you provide in the message content.</li>
            <li><strong>Comments:</strong> When you leave comments on the site, we collect the data shown in the comments form, and also the visitor&apos;s IP address and browser user agent string to help with spam detection.</li>
          </ul>

          <p><strong>b. Data Collected Automatically:</strong></p>
          <p>Like many other websites, we automatically collect certain information when you visit and navigate the site:</p>
          <ul>
            <li><strong>Log Data:</strong> Your Internet Protocol (IP) address, browser type, browser version, Internet Service Provider (ISP), the pages you visit on our site, the time and date of your visit, and other statistics.</li>
            <li><strong>Cookies and Similar Tracking Technologies:</strong> We use cookies and similar tracking technologies to track activity on our Service and hold certain information.</li>
          </ul>

          <h2>2. Cookies and Web Beacons</h2>
          <p>Cookies are files with a small amount of data which may include an anonymous unique identifier. Cookies are sent to your browser from a website and stored on your device.</p>

          <p>We use cookies to:</p>
          <ul>
            <li>Understand and save user&apos;s preferences for future visits.</li>
            <li>Track and analyze usage and aggregate statistical information from our users.</li>
            <li>Serve and personalize advertisements.</li>
          </ul>

          <p>You can instruct your browser to refuse all cookies or to indicate when a cookie is being sent. However, if you do not accept cookies, you may not be able to use some portions of our website.</p>

          <h2>3. Google AdSense & the DoubleClick DART Cookie</h2>
          <p>We use Google AdSense to display advertisements on SimpMusic.org.</p>
          <ul>
            <li>Google, as a third-party vendor, uses cookies to serve ads on our site.</li>
            <li>Google&apos;s use of the DART cookie enables it to serve ads to our users based on their visit to our site and other sites on the Internet.</li>
            <li>Users may opt out of the use of the DART cookie by visiting the Google ad and content network privacy policy at: <a href="https://policies.google.com/technologies/ads" target="_blank" rel="noopener noreferrer">https://policies.google.com/technologies/ads</a></li>
          </ul>

          <p>We have implemented the following Google Analytics services based on Display Advertising: Demographics and Interests Reporting.</p>

          <p>Third-party vendors, including Google, use first-party cookies (such as the Google Analytics cookies) and third-party cookies (such as the DoubleClick cookie) together to inform, optimize, and serve ads based on someone&apos;s past visits to our website.</p>

          <h2>4. How We Use Your Information</h2>
          <p>We use the information we collect for the following purposes:</p>
          <ul>
            <li>To provide, operate, and maintain our website.</li>
            <li>To improve, personalize, and expand our website.</li>
            <li>To understand and analyze how you use our website.</li>
            <li>To communicate with you, respond to inquiries, and provide updates.</li>
            <li>To detect and prevent fraud.</li>
            <li>For compliance purposes, such as enforcing our terms of service or complying with legal obligations.</li>
          </ul>

          <h2>5. Your GDPR Rights</h2>
          <p>If you are a resident of the European Economic Area (EEA), you have certain data protection rights. SimpMusic.org aims to take reasonable steps to allow you to correct, amend, delete, or limit the use of your Personal Data.</p>

          <p>Your rights include:</p>
          <ul>
            <li><strong>The right to access:</strong> You have the right to request copies of your personal information that we hold.</li>
            <li><strong>The right to rectification:</strong> You have the right to request that we correct any information you believe is inaccurate.</li>
            <li><strong>The right to erasure (The right to be forgotten):</strong> You have the right to request that we erase your personal data, under certain conditions.</li>
            <li><strong>The right to restrict processing:</strong> You have the right to request that we restrict the processing of your personal data.</li>
            <li><strong>The right to object to processing:</strong> You have the right to object to our processing of your personal data.</li>
            <li><strong>The right to data portability:</strong> You have the right to request that we transfer the data that we have collected to another organization, or directly to you.</li>
          </ul>

          <p>To exercise any of these rights, please contact us via email: ndtminh2608@gmail.com.</p>

          <h2>6. Data Security</h2>
          <p>The security of your data is important to us. We use reasonable administrative, technical, and physical security measures to help protect your personal information. However, please be aware that no method of transmission over the Internet or method of electronic storage is 100% secure.</p>

          <h2>7. Children&apos;s Privacy</h2>
          <p>Our Service does not address anyone under the age of 13 (&quot;Children&quot;). We do not knowingly collect personally identifiable information from anyone under the age of 13. If you are a parent or guardian and you are aware that your Children have provided us with Personal Data, please contact us.</p>

          <h2>8. Links to Other Websites</h2>
          <p>Our website may contain links to other sites that are not operated by us. If you click on a third-party link, you will be directed to that third party&apos;s site. We strongly advise you to review the Privacy Policy of every site you visit. We have no control over and assume no responsibility for the content, privacy policies, or practices of any third-party sites or services.</p>

          <h2>9. Changes to This Privacy Policy</h2>
          <p>We may update our Privacy Policy from time to time. We will notify you of any changes by posting the new Privacy Policy on this page and updating the &quot;Effective Date&quot; at the top.</p>

          <p>You are advised to review this Privacy Policy periodically for any changes.</p>

          <h2>10. Contact Us</h2>
          <p>If you have any questions about this Privacy Policy, please contact us:</p>
          <p>By email: <a href="mailto:ndtminh2608@gmail.com">ndtminh2608@gmail.com</a></p>
        </div>

        <div className="mt-8">
          <Link href="/" className="text-blue-500 hover:underline inline-flex items-center">
            <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            Back to home
          </Link>
        </div>
      </div>
    </div>
  );
}