"""
Vercel Serverless Function - Health Check
"""
from http.server import BaseHTTPRequestHandler
import firebase_admin
import json

class handler(BaseHTTPRequestHandler):
    def do_GET(self):
        try:
            firebase_status = 'initialized' if firebase_admin._apps else 'not initialized'
            
            response_data = {
                'status': 'ok',
                'firebase': firebase_status,
                'environment': 'vercel'
            }
            
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            self.wfile.write(json.dumps(response_data).encode())
            
        except Exception as e:
            self.send_response(500)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            error_data = {
                'status': 'error',
                'error': str(e)
            }
            self.wfile.write(json.dumps(error_data).encode())
