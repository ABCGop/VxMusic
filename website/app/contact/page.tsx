'use client';

import { Mail, Github, MessageSquare, Heart, Shield } from 'lucide-react';

export default function Contact() {
  return (
    <div className="flex-1 container mx-auto px-6 pt-32 pb-20 max-w-4xl">
      <div className="mb-12 border-b border-white/10 pb-8">
          <h1 className="text-4xl md:text-5xl font-bold mb-6 title-glow">Contact VxMusic</h1>
          <p className="text-xl text-gray-400 leading-relaxed max-w-2xl">
              Thank you for your interest in VxMusic. We welcome feedback, bug reports, feature requests, and contributions.
          </p>
      </div>
      
      <div className="grid gap-8">
          
          <ContactCard 
              icon={<Mail className="w-6 h-6" />}
              title="General Inquiries & Feedback"
              action={<a href="mailto:opabcg@gmail.com" className="text-indigo-400 hover:text-white transition font-medium">opabcg@gmail.com</a>}
          >
              For general questions about VxMusic or to share feedback about the app experience, please email the maintainer. We read all messages and try to respond to important issues.
          </ContactCard>

          <ContactCard 
              icon={<Github className="w-6 h-6" />}
              title="Bugs & Feature Requests"
              action={<a href="https://github.com/ABCGop" target="_blank" className="text-indigo-400 hover:text-white transition font-medium">Open GitHub Issues</a>}
          >
              The fastest way to report bugs or request new features is through the GitHub repository. Please include the app version, a description of the problem, and screenshots when possible.
          </ContactCard>

          <ContactCard 
              icon={<Heart className="w-6 h-6" />}
              title="Contributing"
              action={<a href="https://github.com/ABCGop" target="_blank" className="text-indigo-400 hover:text-white transition font-medium">View Repository</a>}
          >
              VxMusic is open-source — contributions are welcome. Whether you want to fix a bug, translate the app, or submit design improvements, please open a pull request on GitHub.
          </ContactCard>

          <div className="grid md:grid-cols-2 gap-8">
              <div className="p-8 rounded-2xl bg-white/5 border border-white/5">
                  <h3 className="text-xl font-bold text-white mb-4">Press & Partnerships</h3>
                  <p className="text-gray-400 mb-4">
                      For press inquiries or partnership opportunities, email the project lead with a brief description of the proposal.
                  </p>
                  <a href="mailto:opabcg@gmail.com" className="text-indigo-400 hover:text-white transition font-medium">Contact Lead &rarr;</a>
              </div>

              <div className="p-8 rounded-2xl bg-white/5 border border-white/5">
                  <div className="flex items-center gap-3 mb-4">
                      <Shield className="w-5 h-5 text-indigo-400" />
                      <h3 className="text-xl font-bold text-white">Privacy & Safety</h3>
                  </div>
                  <p className="text-gray-400 text-sm">
                      We value user privacy. If your message contains sensitive information, please avoid sharing passwords or private keys. Refer to our Privacy Policy for details.
                  </p>
              </div>
          </div>

          <div className="p-6 rounded-xl border border-white/10 bg-[#0a0a0a] text-center text-sm text-gray-500">
              <strong className="text-gray-300 block mb-1">Mailing Address</strong>
              VxMusic is a remote, community-driven project. Contact us via email for physical mailing instructions.
          </div>

      </div>
    </div>
  );
}

function ContactCard({ icon, title, children, action }: { icon: any, title: string, children: React.ReactNode, action: React.ReactNode }) {
    return (
        <div className="p-8 rounded-3xl bg-[#0a0a0a] border border-white/10 flex flex-col md:flex-row gap-6 items-start hover:border-white/20 transition duration-500">
            <div className="p-3 rounded-xl bg-white/5 text-indigo-400 shrink-0">
                {icon}
            </div>
            <div className="flex-1">
                <h3 className="text-2xl font-bold text-white mb-3">{title}</h3>
                <p className="text-gray-400 leading-relaxed mb-4">
                    {children}
                </p>
                <div className="inline-block">
                    {action}
                </div>
            </div>
        </div>
    )
}
