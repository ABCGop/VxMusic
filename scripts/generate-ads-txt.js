#!/usr/bin/env node

const fs = require("fs");
const path = require("path");

// Get the AdSense publisher ID from environment variables
const publisherId = process.env.NEXT_PUBLIC_ADSENSE_PUBLISHER_ID;

if (!publisherId) {
  console.error(
    "Error: NEXT_PUBLIC_ADSENSE_PUBLISHER_ID environment variable is not set."
  );
  process.exit(1);
}

// Create the content for ads.txt
let adsContent = `google.com, ${publisherId}, DIRECT, f08c47fec0942fa0`;
// Define the path to the ads.txt file
const adsFilePath = path.join(process.cwd(), "public", "ads.txt");

// Write the content to the ads.txt file
fs.writeFileSync(adsFilePath, adsContent);

console.log(`Successfully generated ads.txt with publisher ID: ${publisherId}`);
