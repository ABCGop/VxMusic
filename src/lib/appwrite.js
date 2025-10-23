import { Client, Databases, Query } from 'appwrite';

if (!process.env.NEXT_PUBLIC_APPWRITE_ENDPOINT) {
  throw new Error('Missing NEXT_PUBLIC_APPWRITE_ENDPOINT environment variable');
}

if (!process.env.NEXT_PUBLIC_APPWRITE_PROJECT_ID) {
  throw new Error('Missing NEXT_PUBLIC_APPWRITE_PROJECT_ID environment variable');
}

if (!process.env.NEXT_PUBLIC_APPWRITE_DATABASE_ID) {
  throw new Error('Missing NEXT_PUBLIC_APPWRITE_DATABASE_ID environment variable');
}

if (!process.env.NEXT_PUBLIC_APPWRITE_COLLECTION_ID) {
  throw new Error('Missing NEXT_PUBLIC_APPWRITE_COLLECTION_ID environment variable');
}

const client = new Client()
    .setEndpoint(process.env.NEXT_PUBLIC_APPWRITE_ENDPOINT)
    .setProject(process.env.NEXT_PUBLIC_APPWRITE_PROJECT_ID);

const databases = new Databases(client);

export const DATABASE_ID = process.env.NEXT_PUBLIC_APPWRITE_DATABASE_ID;
export const COLLECTION_ID = process.env.NEXT_PUBLIC_APPWRITE_COLLECTION_ID;

export { databases, Query }; 