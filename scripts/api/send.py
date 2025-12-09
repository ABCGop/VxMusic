"""
Vercel Serverless Function - Send Notification
"""
from http.server import BaseHTTPRequestHandler
import firebase_admin
from firebase_admin import credentials, messaging
import json
import os

# Initialize Firebase Admin SDK
def init_firebase():
    if not firebase_admin._apps:
        service_account_json = os.environ.get('FIREBASE_SERVICE_ACCOUNT')
        if service_account_json:
            service_account = json.loads(service_account_json)
            cred = credentials.Certificate(service_account)
            firebase_admin.initialize_app(cred)

class handler(BaseHTTPRequestHandler):
    def _send_response(self, status_code, data):
        self.send_response(status_code)
        self.send_header('Content-type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        self.end_headers()
        self.wfile.write(json.dumps(data).encode())

    def do_OPTIONS(self):
        self.send_response(200)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        self.end_headers()

    def do_POST(self):
        try:
            init_firebase()
            
            # Read request body
            content_length = int(self.headers.get('Content-Length', 0))
            post_data = self.rfile.read(content_length)
            data = json.loads(post_data.decode('utf-8'))
            
            # Validate required fields
            if not data.get('title') or not data.get('body'):
                self._send_response(400, {
                    'success': False,
                    'error': 'Title and body are required'
                })
                return
            
            # Build notification
            notification = messaging.Notification(
                title=data['title'],
                body=data['body'],
                image=data.get('imageUrl') if data.get('imageUrl') else None
            )
            
            # Determine target
            send_type = data.get('sendType', 'all')
            topic = data.get('topic', 'all_users')
            target_topic = topic if send_type == 'topic' else 'all_users'
            
            # Build message data (all values must be strings)
            message_data = {
                'click_action': str(data.get('deepLink', 'vxmusic://home')),
                'type': str(send_type),
                'topic_name': str(target_topic if send_type == 'topic' else 'broadcast')
            }
            
            if data.get('imageUrl'):
                message_data['image_url'] = str(data['imageUrl'])
            
            # Build message
            message = messaging.Message(
                notification=notification,
                topic=target_topic,
                data=message_data,
                android=messaging.AndroidConfig(
                    priority='high',
                    notification=messaging.AndroidNotification(
                        icon='ic_notification',
                        color='#6200EE',
                        sound='default',
                        channel_id='default'
                    )
                )
            )
            
            # Send message
            response = messaging.send(message)
            
            self._send_response(200, {
                'success': True,
                'messageId': response,
                'message': f'Notification sent to {target_topic}!'
            })
            
        except Exception as e:
            self._send_response(500, {
                'success': False,
                'error': str(e)
            })
