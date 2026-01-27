import { Request, Response } from "express";

export interface ProxyOptions {
    targetUrl: string;
    method?: string;
    body?: any;
    headers?: Record<string, string>;
    fallbackData?: any;
    fallbackStatus?: number;
    onSuccess?: (data: any) => any;
    onError?: (error: any) => void;
}

/**
 * Proxy a request to the backend with automatic fallback in development
 * @param req Express request object
 * @param res Express response object
 * @param options Proxy options including target URL and fallback data
 */
export async function proxyWithFallback(
    req: Request,
    res: Response,
    options: ProxyOptions
): Promise<void> {
    const isDevelopment = process.env.NODE_ENV !== 'production';
    const {
        targetUrl,
        method = req.method,
        body = req.body,
        headers = {},
        fallbackData,
        fallbackStatus = 200,
        onSuccess,
        onError
    } = options;

    try {
        // Attempt to proxy to the real backend
        console.log(`[PROXY] ${method} ${targetUrl}`);
        
        const fetchOptions: RequestInit = {
            method,
            headers: {
                'Content-Type': 'application/json',
                ...headers
            }
        };

        // Only includes a body for POST, PUT, PATCH requests
        if (body && ['POST', 'PUT', 'PATCH'].includes(method.toUpperCase())) {
            fetchOptions.body = JSON.stringify(body);
        }

        const response = await fetch(targetUrl, fetchOptions);
        
        if (!response.ok) {
            throw new Error(`Backend returned ${response.status}: ${response.statusText}`);
        }

        const data = await response.json();
        console.log(`[PROXY] Success: ${method} ${targetUrl}`);
        
        // Apply success transformation if callback function provided, otherwise return raw data
        const finalData = onSuccess ? onSuccess(data) : data;
        
        res.status(response.status).json(finalData);
    } catch (error) {
        console.error(`[PROXY] Error: ${method} ${targetUrl}`, error);
        
        // Call error handler if provided
        if (onError) {
            onError(error);
        }

        // In development, fallback to mock data
        if (isDevelopment && fallbackData !== undefined) {
            console.log(`[FALLBACK] Using mock data for ${method} ${targetUrl}`);
            res.status(fallbackStatus).json(fallbackData);
        } else {
            // In production or if no fallback, return error
            res.status(502).json({
                error: 'Backend service unavailable',
                message: error instanceof Error ? error.message : 'Unknown error',
                timestamp: new Date().toISOString()
            });
        }
    }
}

/**
 * Simple fetch with fallback - doesn't send response, just returns data
 * Useful for internal backend calls
 */
export async function fetchWithFallback<T>(
    url: string,
    fallbackData: T,
    options: RequestInit = {}
): Promise<T> {
    const isDevelopment = process.env.NODE_ENV !== 'production';
    
    try {
        console.log(`[FETCH] ${options.method || 'GET'} ${url}`);
        const response = await fetch(url, options);
        
        if (!response.ok) {
            throw new Error(`Backend returned ${response.status}: ${response.statusText}`);
        }
        
        const data = await response.json();
        console.log(`[FETCH] Success: ${options.method || 'GET'} ${url}`);
        return data as T;
    } catch (error) {
        console.error(`[FETCH] Error: ${options.method || 'GET'} ${url}`, error);
        
        if (isDevelopment) {
            console.log(`[FALLBACK] Using mock data for ${options.method || 'GET'} ${url}`);
            return fallbackData;
        }
        
        throw error;
    }
}
