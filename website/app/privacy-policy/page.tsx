'use client';

export default function Privacy() {
  return (
    <div className="flex-1 container mx-auto px-6 pt-32 pb-20 max-w-4xl">
      <div className="mb-12 border-b border-white/10 pb-8">
          <h1 className="text-4xl md:text-5xl font-bold mb-4 title-glow">Privacy Policy</h1>
          <p className="text-gray-400">Effective Date: 01-04-2025</p>
      </div>
      
      <div className="space-y-12 text-gray-300 text-lg leading-relaxed">
        <section>
            <p className="mb-4">
                Vishesh Gangwar built the VxMusic app as an Open Source app. This SERVICE is provided by Vishesh Gangwar at no cost and is intended for use as is.
            </p>
            <p className="mb-4">
                This page is used to inform visitors regarding my policies with the collection, use, and disclosure of Personal Information if anyone decided to use my Service. If you choose to use my Service, then you agree to the collection and use of information in relation to this policy.
            </p>
            <p>
                The Personal Information that I collect is used for providing and improving the Service. I will not use or share your information with anyone except as described in this Privacy Policy. The terms used in this Privacy Policy have the same meanings as in our Terms of Service, unless otherwise defined in this Privacy Policy.
            </p>
        </section>

        <Section title="Information Collection and Use">
            <p className="mb-4">
                For a better experience, while using our Service, I may require you to provide us with certain personally identifiable information. The information that I request will be retained on your device and is not collected by me in any way.
            </p>
            <p>
                The app does use third-party services that may collect information used to identify you. Link to the privacy policy of third-party service providers used by the app <a href="https://policies.google.com/privacy" target="_blank" className="text-indigo-400 hover:text-white transition">Google Play Services</a>.
            </p>
        </Section>

        <Section title="Log Data">
            <p className="mb-4">
                Log Data may include information such as your device Internet Protocol ("IP") address, device name, operating system version, the configuration of the app when utilizing my Service, the time and date of your use of the Service, and other statistics.
            </p>
            <p>
                But none of this info leaves your device. It's stored in a log-file on your device only accessible to you.
            </p>
        </Section>

        <Section title="Cookies">
            <p className="mb-4">
                Cookies are files with a small amount of data that are commonly used as anonymous unique identifiers. These are sent to your browser from the websites that you visit and are stored on your device's internal memory.
            </p>
            <p>
                This Service does not use these "cookies" explicitly. However, some services may use cookies for their cause (e.g. improvement) but you can deny them once asked.
            </p>
        </Section>

        <Section title="Advertising, Cookies, and Third-Party Vendors">
            <p className="mb-4">
                This website uses Google AdSense and other third-party vendors to serve ads. These vendors, including Google, use cookies and similar technologies to serve ads based on your prior visits to this website or other websites on the Internet.
            </p>
            <p className="mb-4">
                Third-party vendors, including Google, may use cookies to serve ads based on your visits to this and/or other sites. Google's use of advertising cookies enables it and its partners to serve ads to you based on your visit to this site and/or other sites on the Internet.
            </p>
            <p className="mb-4">
                You may opt out of personalized advertising by visiting <a href="https://adssettings.google.com" target="_blank" className="text-indigo-400 hover:text-white transition">Google Ads Settings</a> or by visiting <a href="http://www.aboutads.info" target="_blank" className="text-indigo-400 hover:text-white transition">www.aboutads.info</a> for other third-party vendors.
            </p>
            <p className="mb-4">
                For more information on how Google uses data when you use our partners’ sites or apps, please visit <a href="https://policies.google.com/technologies/partner-sites" target="_blank" className="text-indigo-400 hover:text-white transition">How Google uses data when you use our partners’ sites or apps</a>.
            </p>
            <p>
                We do not knowingly target or collect data from users under the age of 13 for personalized advertising.
            </p>
        </Section>

        <Section title="Service Providers">
            <p className="mb-4">I may employ third-party companies and individuals due to the following reasons:</p>
            <ul className="list-disc pl-6 space-y-2 text-gray-400 mb-4">
                <li>To facilitate our Service;</li>
                <li>To provide the Service on our behalf;</li>
                <li>To perform Service-related services; or</li>
                <li>To assist us in analyzing how our Service is used.</li>
            </ul>
            <p>
                I want to inform users of this Service that these third parties have access to their Personal Information. The reason is to perform the tasks assigned to them on our behalf. However, they are obligated not to disclose or use the information for any other purpose.
            </p>
        </Section>

        <Section title="Security">
            <p>
                I value your trust in providing us your Personal Information, thus we are striving to use commercially acceptable means of protecting it. But remember that no method of transmission over the internet, or method of electronic storage is 100% secure and reliable, and I cannot guarantee its absolute security.
            </p>
        </Section>

        <Section title="Links to Other Sites">
            <p>
                This Service may contain links to other sites. If you click on a third-party link, you will be directed to that site. Note that these external sites are not operated by me. Therefore, I strongly advise you to review the Privacy Policy of these websites. I have no control over and assume no responsibility for the content, privacy policies, or practices of any third-party sites or services.
            </p>
        </Section>

        <Section title="Children's Privacy">
            <p className="mb-4">
                These Services do not address anyone under the age of 13. I do not knowingly collect personally identifiable information from children under 13 years of age. In the case I discover that a child under 13 has provided me with personal information, I immediately delete this from our servers.
            </p>
            <p>
                If you are a parent or guardian and you are aware that your child has provided us with personal information, please contact me so that I will be able to do the necessary actions.
            </p>
        </Section>

        <Section title="Changes to This Privacy Policy">
            <p>
                I may update our Privacy Policy from time to time. Thus, you are advised to review this page periodically for any changes. I will notify you of any changes by posting the new Privacy Policy on this page.
            </p>
        </Section>

        <Section title="Contact Us">
             If you have any questions or suggestions about my Privacy Policy, do not hesitate to contact me at <a href="mailto:opabcg@gmail.com" className="text-indigo-400 hover:text-indigo-300 transition">opabcg@gmail.com</a>.
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
