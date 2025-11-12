#!/usr/bin/env python3
"""
Firebase Cloud Messaging - Notification Sender
Simple script to send notifications to VxMusic users

Requirements:
    pip install firebase-admin

Setup:
    1. Download serviceAccountKey.json from Firebase Console
    2. Place it in the same directory as this script
    3. Run: python send_notification.py
"""

import firebase_admin
from firebase_admin import credentials, messaging
import sys

# Initialize Firebase Admin SDK
try:
    cred = credentials.Certificate('serviceAccountKey.json')
    firebase_admin.initialize_app(cred)
    print("✓ Firebase initialized successfully")
except Exception as e:
    print(f"✗ Error initializing Firebase: {e}")
    print("\nMake sure you have:")
    print("1. Downloaded serviceAccountKey.json from Firebase Console")
    print("2. Placed it in the same directory as this script")
    sys.exit(1)


def send_to_all_users(title, body, image_url=None):
    """Send notification to all users via 'all_users' topic"""
    
    message = messaging.Message(
        notification=messaging.Notification(
            title=title,
            body=body,
            image=image_url if image_url else None
        ),
        data={
            'type': 'announcement',
            'timestamp': str(int(time.time()))
        },
        topic='all_users'
    )
    
    try:
        response = messaging.send(message)
        print(f"✓ Successfully sent to all users: {response}")
        return response
    except Exception as e:
        print(f"✗ Error sending notification: {e}")
        return None


def send_announcement(title, body):
    """Send announcement to all users"""
    print(f"\nSending announcement...")
    print(f"Title: {title}")
    print(f"Body: {body}")
    
    message = messaging.Message(
        notification=messaging.Notification(
            title=title,
            body=body
        ),
        data={
            'type': 'announcement'
        },
        topic='all_users'
    )
    
    response = messaging.send(message)
    print(f"✓ Sent successfully: {response}")


def send_update_notification(version, features):
    """Send update available notification"""
    print(f"\nSending update notification for v{version}...")
    
    message = messaging.Message(
        notification=messaging.Notification(
            title=f'VxMusic v{version} Available!',
            body=f'New features: {features}'
        ),
        data={
            'type': 'update_available',
            'version': version,
            'click_action': 'UPDATE'
        },
        topic='all_users'
    )
    
    response = messaging.send(message)
    print(f"✓ Sent successfully: {response}")


def send_new_feature(title, body, deep_link=None):
    """Send new feature announcement"""
    print(f"\nSending new feature notification...")
    
    data = {
        'type': 'new_feature',
        'click_action': 'FEATURE'
    }
    
    if deep_link:
        data['deep_link'] = deep_link
    
    message = messaging.Message(
        notification=messaging.Notification(
            title=title,
            body=body
        ),
        data=data,
        topic='all_users'
    )
    
    response = messaging.send(message)
    print(f"✓ Sent successfully: {response}")


def send_to_topic(topic, title, body, notification_type='custom'):
    """Send notification to specific topic"""
    print(f"\nSending to topic '{topic}'...")
    
    message = messaging.Message(
        notification=messaging.Notification(
            title=title,
            body=body
        ),
        data={
            'type': notification_type
        },
        topic=topic
    )
    
    response = messaging.send(message)
    print(f"✓ Sent successfully: {response}")


def send_to_device(token, title, body):
    """Send notification to specific device"""
    print(f"\nSending to device: {token[:20]}...")
    
    message = messaging.Message(
        notification=messaging.Notification(
            title=title,
            body=body
        ),
        data={
            'type': 'announcement'
        },
        token=token
    )
    
    response = messaging.send(message)
    print(f"✓ Sent successfully: {response}")


def interactive_mode():
    """Interactive mode for sending notifications"""
    import time
    
    print("\n" + "="*60)
    print("VxMusic Notification Sender")
    print("="*60)
    
    while True:
        print("\n📱 What would you like to send?")
        print("1. Announcement to all users")
        print("2. Update notification")
        print("3. New feature announcement")
        print("4. Send to specific topic")
        print("5. Send to specific device")
        print("6. Exit")
        
        choice = input("\nEnter choice (1-6): ").strip()
        
        if choice == '1':
            title = input("Enter title: ").strip()
            body = input("Enter body: ").strip()
            send_announcement(title, body)
            
        elif choice == '2':
            version = input("Enter version (e.g., 2.0): ").strip()
            features = input("Enter features: ").strip()
            send_update_notification(version, features)
            
        elif choice == '3':
            title = input("Enter title: ").strip()
            body = input("Enter body: ").strip()
            deep_link = input("Enter deep link (optional, press Enter to skip): ").strip()
            send_new_feature(title, body, deep_link if deep_link else None)
            
        elif choice == '4':
            topic = input("Enter topic name: ").strip()
            title = input("Enter title: ").strip()
            body = input("Enter body: ").strip()
            send_to_topic(topic, title, body)
            
        elif choice == '5':
            token = input("Enter device FCM token: ").strip()
            title = input("Enter title: ").strip()
            body = input("Enter body: ").strip()
            send_to_device(token, title, body)
            
        elif choice == '6':
            print("\n👋 Goodbye!")
            break
        
        else:
            print("❌ Invalid choice. Please try again.")


# Example usage
if __name__ == "__main__":
    import time
    
    # Check command line arguments
    if len(sys.argv) > 1:
        if sys.argv[1] == "--example":
            # Send example notification
            print("Sending example notification...")
            send_announcement(
                "Welcome to VxMusic!",
                "Enjoy unlimited music streaming"
            )
        elif sys.argv[1] == "--update":
            # Send update notification
            send_update_notification("2.0", "Offline mode, better UI, bug fixes")
        else:
            print(f"Unknown argument: {sys.argv[1]}")
            print("Usage: python send_notification.py [--example|--update]")
    else:
        # Interactive mode
        interactive_mode()
