# SimpMusic Lyrics

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Next.js](https://img.shields.io/badge/Next.js-15.4.5-black)](https://nextjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.0-blue)](https://www.typescriptlang.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-CSS-06B6D4)](https://tailwindcss.com/)

> 🎵 **A modern web interface for SimpMusic Lyrics API** - Search, view, and manage song lyrics with a beautiful, responsive UI.


## Features

### **Smart Search**
- **Full-text search** across lyrics content
- **Artist search** to find songs by specific artists  
- **Song title search** for exact matches
- **Real-time search** with instant results
- **Request timing** display for performance transparency

### **Modern UI/UX**
- **Responsive design** optimized for mobile and desktop
- **Dark/Light theme** support with system preference detection
- **Beautiful typography** with optimized font loading
- **Smooth animations** and transitions
- **Clean, minimalist** interface inspired by modern music apps

### **Lyrics Management**
- **Lyrics viewer** with synced and plain lyrics support
- **Community translations** with language chips
- **Vote system** for lyrics quality (upvote/downvote)
- **Download lyrics** as LRC files
- **Contributor attribution** and vote counts
- **Toast notifications** for user feedback

### **Security & Performance**
- **HMAC authentication** for API requests
- **Rate limiting** awareness and error handling
- **Real-time vote updates** without page refresh
- **Optimized API calls** with proper caching
- **Error boundaries** and graceful error handling

## Quick Start

### Prerequisites

- **Node.js** 18+ 
- **npm/yarn/pnpm/bun**

### Installation

```bash
# Clone the repository
git clone https://github.com/maxrave-dev/lyrics-web.git
cd lyrics-web

# Install dependencies
npm install
# or
yarn install
# or
pnpm install
# or
bun install
```

### Development

```bash
# Start development server
npm run dev
# or
yarn dev
# or
pnpm dev
# or
bun dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser.

### Build for Production

```bash
# Build the application
npm run build
# or
yarn build

# Start production server
npm start
# or
yarn start
```

## Tech Stack

### Frontend Framework
- **[Next.js 15.4.5](https://nextjs.org/)** - React framework with App Router
- **[React 19](https://react.dev/)** - UI library
- **[TypeScript](https://www.typescriptlang.org/)** - Type safety

### Styling
- **[Tailwind CSS](https://tailwindcss.com/)** - Utility-first CSS framework
- **[shadcn/ui](https://ui.shadcn.com/)** - High-quality React components

## Related Projects

This web interface connects to the **SimpMusic Lyrics API**:

### **Backend API**
- **Repository**: [maxrave-dev/lyrics](https://github.com/maxrave-dev/lyrics)
- **Live API**: [api-lyrics.simpmusic.org](https://api-lyrics.simpmusic.org)
- **Tech Stack**: Kotlin, Spring Boot, Appwrite Database
- **Features**: RESTful API, HMAC authentication, rate limiting, full-text search

### **SimpMusic App**
- **Repository**: [maxrave-dev/SimpMusic](https://github.com/maxrave-dev/SimpMusic)
- **Platform**: Android (Jetpack Compose)
- **Description**: FOSS YouTube Music client with lyrics integration

## 📝 Project Structure

```
src/
├── app/                 # Next.js App Router
│   ├── about/          # About page
│   ├── api/            # API documentation
│   ├── donate/         # Donation page  
│   ├── search/         # Search page
│   └── layout.tsx      # Root layout
├── components/         # React components
│   ├── ui/            # shadcn/ui components
│   ├── LyricsDialog.tsx # Main lyrics viewer
│   ├── Header.tsx     # Navigation header
│   └── Footer.tsx     # Page footer
├── lib/               # Utilities
│   ├── hmac.ts       # HMAC authentication
│   ├── config.ts     # Configuration
│   └── utils.ts      # General utilities
└── hooks/            # Custom React hooks
```

## Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details.

### Development Workflow

1. **Fork** the repository
2. **Create** a feature branch: `git checkout -b feature/amazing-feature`
3. **Commit** your changes: `git commit -m 'Add amazing feature'`
4. **Push** to the branch: `git push origin feature/amazing-feature`
5. **Open** a Pull Request

### Code Style

- **TypeScript** for type safety
- **ESLint + Prettier** for code formatting
- **Conventional Commits** for commit messages
- **Component-driven** development

## License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

 ## Support & Donations 
 #### Special thanks to all supporter ❤️    
 <div align="left"> 
 <a href="https://simpmusic.org/"><img alt="Visit the website" height="50" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/documentation/website_vector.svg"></a> &nbsp;        
<a href="https://discord.gg/Rq5tWVM9Hg"><img alt="Discord Server" height="50" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/social/discord-plural_vector.svg"></a> &nbsp;        
<br> <a href="https://www.buymeacoffee.com/maxrave"><img alt="Buy me a Coffee" height="50" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/donate/buymeacoffee-singular_vector.svg"></a> &nbsp;        
<a href="https://liberapay.com/maxrave/"><img alt="liberapay" height="50"        
src="https://raw.githubusercontent.com/liberapay/liberapay.com/master/www/assets/liberapay/logo-v2_black-on-yellow.svg"></a> 
</div>
    
 ### MOMO or Vietnamese banking    
 <p float="left">        
 <img src="https://github.com/maxrave-dev/SimpMusic/blob/dev/asset/52770992.jpg?raw=true" width="300"> 
 </p>

## SimpMusic is sponsored by:
<br />
<a href="https://vercel.com/oss">
  <img alt="Vercel OSS Program" src="https://vercel.com/oss/program-badge.svg" />
</a>
<br />
<br />
<a href="https://www.digitalocean.com/?refcode=d7f6eedfb9a9&utm_campaign=Referral_Invite&utm_medium=Referral_Program&utm_source=badge"><img src="https://web-platforms.sfo2.cdn.digitaloceanspaces.com/WWW/Badge%201.svg" width="300" alt="DigitalOcean Referral Badge" /></a>
<br>
<br>
<a href="https://crowdin.com">
<img src="https://support.crowdin.com/assets/logos/plate/png/crowdin-logo-with-plate.png" width="300"/>
</a>
<br>
<a href="https://sentry.io">
<img src="https://github.com/maxrave-dev/SimpMusic/blob/dev/asset/sentry.svg?raw=true" width="300"/>
</a>
<br>
<br>

Get a free $200 credit over 60 days on DigitalOcean: [GET NOW](https://www.digitalocean.com/?refcode=d7f6eedfb9a9&utm_campaign=Referral_Invite&utm_medium=Referral_Program&utm_source=badge)

Crowdin and Sentry both have a free enterprise plan for Open-source projects. Follow the URLs: 
- [Open Source License Request Form | Crowdin](https://crowdin.com/page/open-source-project-setup-request)
- [Sentry for Open Source | Sentry](https://sentry.io/for/open-source/)

Check out the Vercel open-source program:
- https://vercel.com/open-source-program

*This project is a part of SimpMusic.org Open-source project by me [maxrave-dev](https://github.com/maxrave-dev)*