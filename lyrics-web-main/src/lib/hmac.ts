import { HMAC_SECRET } from './config';

/**
 * Generate HMAC token for API authentication
 * Based on: https://github.com/maxrave-dev/lyrics/blob/main/src/main/kotlin/org/simpmusic/lyrics/infrastructure/config/HmacService.kt
 */
export async function generateHmacToken(requestUri: string): Promise<{ timestamp: string; hmac: string }> {
  const timestamp = Date.now().toString();
  const data = timestamp + requestUri;
  
  console.log('HMAC Debug:', {
    timestamp,
    requestUri,
    data,
    secret: HMAC_SECRET,
    algorithm: 'HmacSHA256'
  });
  
  // Create HMAC using Web Crypto API - matching Kotlin implementation
  const encoder = new TextEncoder();
  const keyData = encoder.encode(HMAC_SECRET);
  const messageData = encoder.encode(data);
  
  const cryptoKey = await crypto.subtle.importKey(
    'raw',
    keyData,
    { name: 'HMAC', hash: 'SHA-256' },
    false,
    ['sign']
  );
  
  const signature = await crypto.subtle.sign('HMAC', cryptoKey, messageData);
  
  // Use Base64 encoding like Kotlin implementation
  const hmac = btoa(String.fromCharCode(...new Uint8Array(signature)));
  
  console.log('Generated HMAC (Base64):', hmac);
    
  return { timestamp, hmac };
}

/**
 * Add HMAC authentication headers to request
 */
export async function addHmacHeaders(
  headers: Record<string, string>,
  requestUri: string
): Promise<Record<string, string>> {
  const { timestamp, hmac } = await generateHmacToken(requestUri);
  
  return {
    ...headers,
    'X-Timestamp': timestamp,
    'X-HMAC': hmac,
  };
}
