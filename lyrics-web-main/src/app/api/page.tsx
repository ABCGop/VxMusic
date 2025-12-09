"use client";

import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { Loader2, Copy, Check } from 'lucide-react';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { tomorrow } from 'react-syntax-highlighter/dist/esm/styles/prism';

interface OpenAPISpec {
  openapi: string;
  info: {
    title: string;
    version: string;
    description?: string;
  };
  servers?: Array<{
    url: string;
    description?: string;
  }>;
  paths: Record<string, PathItem>;
  components?: {
    schemas?: Record<string, Schema>;
  };
}

interface PathItem {
  get?: Operation;
  post?: Operation;
  put?: Operation;
  delete?: Operation;
  patch?: Operation;
}

interface Operation {
  summary?: string;
  description?: string;
  parameters?: Parameter[];
  requestBody?: RequestBody;
  responses: Record<string, Response>;
  tags?: string[];
}

interface Parameter {
  name: string;
  in: 'query' | 'path' | 'header' | 'cookie';
  required?: boolean;
  schema?: Schema;
  description?: string;
}

interface RequestBody {
  description?: string;
  content?: Record<string, MediaType>;
  required?: boolean;
}

interface Response {
  description: string;
  content?: Record<string, MediaType>;
}

interface MediaType {
  schema?: Schema;
}

interface Schema {
  type?: string;
  format?: string;
  properties?: Record<string, Schema>;
  items?: Schema;
  $ref?: string;
  enum?: string[];
  example?: any;
  description?: string;
}

export default function ApiPage() {
  const [spec, setSpec] = useState<OpenAPISpec | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [copied, setCopied] = useState<string | null>(null);

  useEffect(() => {
    loadOpenAPISpec();
  }, []);

  const loadOpenAPISpec = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await fetch('/openapi.json');
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data = await response.json();
      setSpec(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load OpenAPI spec');
      setSpec(null);
    } finally {
      setLoading(false);
    }
  };

  const copyToClipboard = async (text: string, key: string) => {
    try {
      await navigator.clipboard.writeText(text);
      setCopied(key);
      setTimeout(() => setCopied(null), 2000);
    } catch (err) {
      console.error('Failed to copy:', err);
    }
  };

  const getMethodColor = (method: string) => {
    const colors = {
      get: 'bg-green-500',
      post: 'bg-blue-500',
      put: 'bg-yellow-500',
      delete: 'bg-red-500',
      patch: 'bg-purple-500',
    };
    return colors[method as keyof typeof colors] || 'bg-gray-500';
  };

  const renderSchema = (schema: Schema, depth = 0): React.ReactNode => {
    if (schema.$ref) {
      const refName = schema.$ref.split('/').pop();
      return <span className="text-blue-600 dark:text-blue-400">${refName}</span>;
    }

    if (schema.type === 'object' && schema.properties) {
      return (
        <div className={`ml-${depth * 4}`}>
          <div className="font-mono text-sm">
            {'{'}
            {Object.entries(schema.properties).map(([key, value]) => (
              <div key={key} className="ml-4">
                <span className="text-green-600 dark:text-green-400">{key}</span>
                {value.type && <span className="text-gray-500 dark:text-gray-400">: {value.type}</span>}
                {value.format && <span className="text-gray-400 dark:text-gray-500">({value.format})</span>}
                {value.enum && <span className="text-gray-400"> = {value.enum.join(' | ')}</span>}
                {value.description && <span className="text-gray-600 dark:text-gray-400 ml-2">// {value.description}</span>}
                {value.properties && <div className="mt-2">{renderSchema(value, depth + 1)}</div>}
              </div>
            ))}
            {'}'}
          </div>
        </div>
      );
    }

    if (schema.type === 'array' && schema.items) {
      return (
        <span>
          {'array<'}
          {renderSchema(schema.items, depth)}
          {'>'}
        </span>
      );
    }

    return <span>{schema.type}</span>;
  };

  const resolveRef = (ref: string): Schema | null => {
    if (!spec || !spec.components || !spec.components.schemas) return null;
    
    const refName = ref.split('/').pop();
    if (!refName) return null;
    
    return spec.components.schemas[refName] || null;
  };

  const renderSchemaJson = (schema: Schema, processedRefs = new Set<string>()): any => {
    if (schema.$ref) {
      if (processedRefs.has(schema.$ref)) {
        return { $ref: schema.$ref };
      }
      
      const resolvedSchema = resolveRef(schema.$ref);
      if (resolvedSchema) {
        processedRefs.add(schema.$ref);
        const result = renderSchemaJson(resolvedSchema, processedRefs);
        processedRefs.delete(schema.$ref);
        return result;
      }
      return { $ref: schema.$ref };
    }

    if (schema.type === 'object' && schema.properties) {
      const result: Record<string, any> = {};
      Object.entries(schema.properties).forEach(([key, value]) => {
        result[key] = renderSchemaJson(value, processedRefs);
      });
      return result;
    }

    if (schema.type === 'array' && schema.items) {
      return [renderSchemaJson(schema.items, processedRefs)];
    }

    return {
      type: schema.type,
      format: schema.format,
      enum: schema.enum,
      description: schema.description,
      example: schema.example
    };
  };

  const renderExample = (schema: Schema) => {
    if (schema.$ref) {
      const resolvedSchema = resolveRef(schema.$ref);
      if (resolvedSchema) {
        return JSON.stringify(renderSchemaJson(resolvedSchema), null, 2);
      }
    }

    if (schema.example) {
      return JSON.stringify(schema.example, null, 2);
    }
    
    if (schema.type === 'object' && schema.properties) {
      const example: Record<string, any> = {};
      Object.entries(schema.properties).forEach(([key, value]) => {
        if (value.$ref) {
          const resolvedValue = resolveRef(value.$ref);
          if (resolvedValue) {
            example[key] = renderSchemaJson(resolvedValue);
          }
        } else if (value.example) {
          example[key] = value.example;
        } else if (value.type === 'string') {
          example[key] = 'string';
        } else if (value.type === 'number' || value.type === 'integer') {
          example[key] = 0;
        } else if (value.type === 'boolean') {
          example[key] = true;
        } else if (value.type === 'array' && value.items) {
          example[key] = [];
        } else if (value.type === 'object') {
          example[key] = {};
        }
      });
      return JSON.stringify(example, null, 2);
    }
    
    if (schema.type === 'array' && schema.items) {
      if (schema.items.$ref) {
        const resolvedItems = resolveRef(schema.items.$ref);
        if (resolvedItems) {
          return JSON.stringify([renderSchemaJson(resolvedItems)], null, 2);
        }
      }
      return JSON.stringify([renderSchemaJson(schema.items)], null, 2);
    }
    
    return JSON.stringify(renderSchemaJson(schema), null, 2);
  };

  if (loading) {
    return (
      <div className="container mx-auto py-8 px-4 max-w-7xl pt-24">
        <div className="flex items-center justify-center h-64">
          <Loader2 className="h-8 w-8 animate-spin" />
          <span className="ml-2">Loading API documentation...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto py-8 px-4 max-w-7xl pt-24">
      <div className="mb-8">
        <h1 className="text-4xl font-bold mb-4">SimpMusic Lyrics API Documentation</h1>
        <p className="text-lg text-gray-600 dark:text-gray-400">
          Interactive API documentation generated from OpenAPI specification
        </p>
      </div>

      <div className="space-y-6 mb-8">
        <Card>
          <CardHeader>
            <CardTitle>Rate Limiting</CardTitle>
            <CardDescription>
              The API is protected with rate limiting to prevent abuse
            </CardDescription>
          </CardHeader>
          <CardContent>
            <ul className="space-y-2 text-sm">
              <li>• <strong>30 requests per minute</strong> per IP address</li>
              <li>• Applies to <strong>all API endpoints</strong></li>
              <li>• When limit is exceeded, returns <strong>HTTP 429 (Too Many Requests)</strong></li>
            </ul>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>HMAC Authentication</CardTitle>
            <CardDescription>
              All non-GET requests (POST, PUT, DELETE, PATCH) require HMAC authentication
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4 text-sm">
              <div>
                <h4 className="font-semibold mb-2">Steps to generate HMAC:</h4>
                <ol className="space-y-1 list-decimal list-inside">
                  <li>Generate a timestamp (current time in milliseconds)</li>
                  <li>Create data string: <code className="bg-gray-100 dark:bg-gray-800 px-2 py-1 rounded">timestamp + request_uri</code></li>
                  <li>Generate HMAC using the shared secret key</li>
                  <li>Add headers to your request:
                    <ul className="ml-4 mt-1 space-y-1">
                      <li>• <code className="bg-gray-100 dark:bg-gray-800 px-2 py-1 rounded">X-Timestamp: Your timestamp</code></li>
                      <li>• <code className="bg-gray-100 dark:bg-gray-800 px-2 py-1 rounded">X-HMAC: Your generated HMAC token</code></li>
                    </ul>
                  </li>
                </ol>
              </div>
              
              <div>
                <h4 className="font-semibold mb-2">Configuration:</h4>
                <ul className="space-y-1">
                  <li>• HMAC secret: <code className="bg-gray-100 dark:bg-gray-800 px-2 py-1 rounded">simpmusic-lyrics</code></li>
                  <li>• Your HMAC token is available in 5 minutes</li>
                </ul>
              </div>

              <div>
                <h4 className="font-semibold mb-2">References:</h4>
                <ul className="space-y-1">
                  <li>• <a href="https://github.com/maxrave-dev/lyrics/blob/main/src/main/kotlin/org/simpmusic/lyrics/presentation/controller/SecurityController.kt"
                         className="text-blue-600 hover:underline" target="_blank" rel="noopener noreferrer">
                      HMAC generation example
                    </a>
                  </li>
                  <li>• <a href="https://github.com/maxrave-dev/lyrics/blob/main/src/main/kotlin/org/simpmusic/lyrics/infrastructure/config/HmacService.kt"
                         className="text-blue-600 hover:underline" target="_blank" rel="noopener noreferrer">
                      HmacService.kt - Full algorithm
                    </a>
                  </li>
                </ul>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {error && (
        <Alert variant="destructive" className="mb-8">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {spec && (
        <div className="space-y-8">
          <Card>
            <CardHeader>
              <CardTitle>{spec.info.title}</CardTitle>
              <CardDescription>
                Version: {spec.info.version}
                {spec.info.description && (
                  <p className="mt-2">{spec.info.description}</p>
                )}
              </CardDescription>
            </CardHeader>
            {spec.servers && spec.servers.length > 0 && (
              <CardContent>
                <h3 className="font-semibold mb-2">Servers</h3>
                <div className="space-y-2">
                  {spec.servers.map((server, index) => (
                    <div key={index} className="flex items-center gap-2">
                      <code className="px-2 py-1 bg-gray-100 dark:bg-gray-800 rounded">
                        {server.url}
                      </code>
                      {server.description && (
                        <span className="text-sm text-gray-600 dark:text-gray-400">
                          {server.description}
                        </span>
                      )}
                    </div>
                  ))}
                </div>
              </CardContent>
            )}
          </Card>

          <div className="space-y-6">
            <h2 className="text-2xl font-bold">Endpoints</h2>
            {Object.entries(spec.paths).map(([path, pathItem]) => (
              <Card key={path} className="overflow-hidden">
                <CardHeader>
                  <div className="flex items-center gap-4">
                    <code className="text-lg font-mono">{path}</code>
                  </div>
                </CardHeader>
                <CardContent>
                  <Tabs defaultValue={Object.keys(pathItem)[0]} className="w-full">
                    <TabsList className="grid grid-cols-5">
                      {Object.entries(pathItem).map(([method]) => (
                        <TabsTrigger key={method} value={method} className="capitalize">
                          {method}
                        </TabsTrigger>
                      ))}
                    </TabsList>
                    
                    {Object.entries(pathItem).map(([method, operation]) => {
                      if (!operation) return null;
                      
                      return (
                        <TabsContent key={method} value={method} className="space-y-4">
                          <div className="flex items-center gap-2">
                            <Badge className={`${getMethodColor(method)} capitalize`}>
                              {method}
                            </Badge>
                            <span className="font-mono text-sm">{path}</span>
                          </div>
                          
                          {operation.summary && (
                            <p className="text-lg font-semibold">{operation.summary}</p>
                          )}
                          
                          {operation.description && (
                            <p className="text-gray-600 dark:text-gray-400">
                              {operation.description}
                            </p>
                          )}

                          {operation.parameters && operation.parameters.length > 0 && (
                            <div>
                              <h4 className="font-semibold mb-2">Parameters</h4>
                              <div className="space-y-2">
                                {operation.parameters.map((param: Parameter, index: number) => (
                                  <div key={index} className="border rounded p-3">
                                    <div className="flex items-center gap-2">
                                      <code className="font-mono">{param.name}</code>
                                      <Badge variant="outline">{param.in}</Badge>
                                      {param.required && <Badge>Required</Badge>}
                                    </div>
                                    {param.description && (
                                      <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                                        {param.description}
                                      </p>
                                    )}
                                    {param.schema && (
                                      <div className="mt-1">
                                        <code className="text-sm">
                                          {renderSchema(param.schema)}
                                        </code>
                                      </div>
                                    )}
                                  </div>
                                ))}
                              </div>
                            </div>
                          )}

                          {operation.requestBody && (
                            <div>
                              <h4 className="font-semibold mb-2">Request Body</h4>
                              {operation.requestBody.description && (
                                <p className="text-sm text-gray-600 dark:text-gray-400 mb-2">
                                  {operation.requestBody.description}
                                </p>
                              )}
                              {operation.requestBody.content && (
                                <Tabs defaultValue={Object.keys(operation.requestBody.content)[0]}>
                                  <TabsList>
                                    {Object.keys(operation.requestBody.content).map(contentType => (
                                      <TabsTrigger key={contentType} value={contentType}>
                                        {contentType}
                                      </TabsTrigger>
                                    ))}
                                  </TabsList>
                                  {Object.entries(operation.requestBody.content).map(([contentType, mediaType]) => (
                                    <TabsContent key={contentType} value={contentType}>
                                      {(mediaType as MediaType).schema && (
                                        <div>
                                          <div className="flex justify-between items-center mb-2">
                                            <h5 className="font-medium">Schema</h5>
                                            <Button
                                              size="sm"
                                              variant="ghost"
                                              onClick={() => copyToClipboard(
                                                JSON.stringify((mediaType as MediaType).schema, null, 2),
                                                `request-${path}-${method}-${contentType}`
                                              )}
                                            >
                                              {copied === `request-${path}-${method}-${contentType}` ? (
                                                <Check className="h-4 w-4" />
                                              ) : (
                                                <Copy className="h-4 w-4" />
                                              )}
                                            </Button>
                                          </div>
                                          <SyntaxHighlighter
                                            language="json"
                                            style={tomorrow}
                                            className="rounded"
                                          >
                                            {renderExample((mediaType as MediaType).schema!)}
                                          </SyntaxHighlighter>
                                        </div>
                                      )}
                                    </TabsContent>
                                  ))}
                                </Tabs>
                              )}
                            </div>
                          )}

                          {operation.responses && (
                            <div>
                              <h4 className="font-semibold mb-2">Responses</h4>
                              <div className="space-y-2">
                                {Object.entries(operation.responses).map(([status, response]) => (
                                  <div key={status} className="border rounded p-3">
                                    <div className="flex items-center gap-2">
                                      <Badge variant={status.startsWith('2') ? 'default' : 'secondary'}>
                                        {status}
                                      </Badge>
                                      <span>{(response as Response).description}</span>
                                    </div>
                                    {(response as Response).content && (
                                      <Tabs defaultValue={Object.keys((response as Response).content!)[0]} className="mt-2">
                                        <TabsList>
                                          {Object.keys((response as Response).content!).map(contentType => (
                                            <TabsTrigger key={contentType} value={contentType}>
                                              {contentType}
                                            </TabsTrigger>
                                          ))}
                                        </TabsList>
                                        {Object.entries((response as Response).content!).map(([contentType, mediaType]) => (
                                          <TabsContent key={contentType} value={contentType}>
                                            {(mediaType as MediaType).schema && (
                                              <div>
                                                <div className="flex justify-between items-center mb-2">
                                                  <h5 className="font-medium">Schema</h5>
                                                  <Button
                                                    size="sm"
                                                    variant="ghost"
                                                    onClick={() => copyToClipboard(
                                                      JSON.stringify((mediaType as MediaType).schema, null, 2),
                                                      `response-${path}-${method}-${status}-${contentType}`
                                                    )}
                                                  >
                                                    {copied === `response-${path}-${method}-${status}-${contentType}` ? (
                                                      <Check className="h-4 w-4" />
                                                    ) : (
                                                      <Copy className="h-4 w-4" />
                                                    )}
                                                  </Button>
                                                </div>
                                                <SyntaxHighlighter
                                                  language="json"
                                                  style={tomorrow}
                                                  className="rounded"
                                                >
                                                  {renderExample((mediaType as MediaType).schema!)}
                                                </SyntaxHighlighter>
                                              </div>
                                            )}
                                          </TabsContent>
                                        ))}
                                      </Tabs>
                                    )}
                                  </div>
                                ))}
                              </div>
                            </div>
                          )}
                        </TabsContent>
                      );
                    })}
                  </Tabs>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}