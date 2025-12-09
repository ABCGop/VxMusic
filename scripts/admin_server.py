from flask import Flask, render_template, request, jsonify, session, redirect, url_for
from flask_cors import CORS
from functools import wraps
import firebase_admin
from firebase_admin import credentials, messaging
import os
from datetime import datetime
from dotenv import load_dotenv
import json

# Load environment variables
load_dotenv()

app = Flask(__name__)
app.secret_key = os.getenv('SECRET_KEY', os.urandom(24))
CORS(app, supports_credentials=True)

# Initialize Firebase Admin SDK
# On Vercel: Use environment variable with JSON credentials
# Locally: Use serviceAccountKey.json file
if os.getenv('VERCEL'):
    # Running on Vercel - load credentials from environment variable
    firebase_creds = os.getenv('FIREBASE_SERVICE_ACCOUNT_KEY')
    if firebase_creds:
        cred_dict = json.loads(firebase_creds)
        cred = credentials.Certificate(cred_dict)
    else:
        raise ValueError("FIREBASE_SERVICE_ACCOUNT_KEY environment variable not set")
else:
    # Running locally - use service account file
    cred = credentials.Certificate('serviceAccountKey.json')

firebase_admin.initialize_app(cred)

# Admin credentials from environment variables
ADMIN_EMAIL = os.getenv('ADMIN_EMAIL')
ADMIN_PASSWORD = os.getenv('ADMIN_PASSWORD')

# In-memory storage for logs and statistics (no database needed)
activity_logs = []
MAX_LOGS = 100

# Statistics tracking
statistics = {
    'server_start_time': datetime.now(),
    'total_notifications_sent': 0,
    'total_popups_sent': 0,
    'total_theme_updates': 0,
    'total_feature_toggles': 0,
    'fcm_success_count': 0,
    'fcm_failure_count': 0,
    'last_activity_time': None
}

def login_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if 'admin_logged_in' not in session:
            return redirect(url_for('login'))
        return f(*args, **kwargs)
    return decorated_function

def add_log(action):
    """Add activity log to in-memory storage"""
    global activity_logs
    log_entry = {
        'action': action,
        'timestamp': int(datetime.now().timestamp() * 1000)
    }
    activity_logs.insert(0, log_entry)
    if len(activity_logs) > MAX_LOGS:
        activity_logs = activity_logs[:MAX_LOGS]
    statistics['last_activity_time'] = datetime.now()
    print(f"[LOG] {action}")

@app.route('/')
def index():
    if 'admin_logged_in' in session:
        return render_template('admin_dashboard.html')
    return redirect(url_for('login'))

@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        data = request.get_json()
        email = data.get('email')
        password = data.get('password')
        
        # Debug logging
        print(f"Login attempt - Email: {email}")
        print(f"Expected email: {ADMIN_EMAIL}")
        print(f"Email match: {email == ADMIN_EMAIL}")
        print(f"Password match: {password == ADMIN_PASSWORD}")
        
        if email == ADMIN_EMAIL and password == ADMIN_PASSWORD:
            session['admin_logged_in'] = True
            session['admin_email'] = email
            add_log(f'Admin logged in: {email}')
            return jsonify({'success': True})
        else:
            return jsonify({'success': False, 'error': 'Invalid credentials'}), 401
    
    return render_template('login.html')

@app.route('/logout')
def logout():
    email = session.get('admin_email', 'Unknown')
    session.clear()
    add_log(f'Admin logged out: {email}')
    return redirect(url_for('login'))

@app.route('/api/users')
@login_required
def get_users():
    """Get all users from Firebase Auth"""
    try:
        from firebase_admin import auth
        # List users (limit to 1000 for this admin panel version)
        users = []
        for user in auth.list_users(max_results=1000).iterate_all():
             users.append({
                 'uid': user.uid,
                 'email': user.email or 'No Email',
                 'display_name': user.display_name or 'No Name',
                 'creation_time': user.user_metadata.creation_timestamp,
                 'last_sign_in': user.user_metadata.last_sign_in_timestamp,
                 'disabled': user.disabled
             })
        return jsonify(users)
    except Exception as e:
        print(f"Error fetching users: {e}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/users/<user_id>/block', methods=['POST'])
@login_required
def block_user(user_id):
    """Block user - Disabled (user management removed)"""
    return jsonify({'error': 'User management disabled'}), 403

@app.route('/api/notifications/send', methods=['POST'])
@login_required
def send_notification():
    """Send notification to all users via FCM topic"""
    try:
        data = request.get_json()
        title = data.get('title')
        msg = data.get('message')
        
        if not title or not msg:
            return jsonify({'error': 'Title and message required'}), 400
        
        # Send FCM message to topic 'all_users'
        message = messaging.Message(
            notification=messaging.Notification(
                title=title,
                body=msg
            ),
            data={
                'type': 'notification',
                'title': title,
                'message': msg,
                'timestamp': str(int(datetime.now().timestamp() * 1000))
            },
            topic='all_users'
        )
        
        response = messaging.send(message)
        statistics['total_notifications_sent'] += 1
        statistics['fcm_success_count'] += 1
        print(f"Sent FCM notification: {response}")
        add_log(f'Sent notification: "{title}"')
        
        return jsonify({'success': True, 'message_id': response})
    except Exception as e:
        statistics['fcm_failure_count'] += 1
        print(f"FCM Error: {str(e)}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/popups/send', methods=['POST'])
@login_required
def send_popup():
    """Send popup to all users via FCM topic"""
    try:
        data = request.get_json()
        popup_type = data.get('type', 'info')
        title = data.get('title')
        msg = data.get('message')
        
        if not title or not msg:
            return jsonify({'error': 'Title and message required'}), 400
        
        # Send FCM message to topic 'all_users'
        message = messaging.Message(
            data={
                'type': 'popup',
                'popup_type': popup_type,
                'title': title,
                'message': msg,
                'timestamp': str(int(datetime.now().timestamp() * 1000))
            },
            topic='all_users'
        )
        
        response = messaging.send(message)
        print(f"Sent FCM popup: {response}")
        add_log(f'Sent popup: "{title}"')
        
        return jsonify({'success': True, 'message_id': response})
    except Exception as e:
        print(f"FCM Error: {str(e)}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/theme/update', methods=['POST'])
@login_required
def update_theme():
    """Update app theme colors via FCM"""
    try:
        data = request.get_json()
        primary_color = data.get('primary_color')
        secondary_color = data.get('secondary_color')
        accent_color = data.get('accent_color')
        background_color = data.get('background_color')
        surface_color = data.get('surface_color')
        error_color = data.get('error_color')
        
        if not primary_color or not secondary_color:
            return jsonify({'error': 'Colors required'}), 400
        
        # Build message data with all colors
        message_data = {
            'type': 'theme_update',
            'primary_color': primary_color,
            'secondary_color': secondary_color,
            'accent_color': accent_color or secondary_color,
            'timestamp': str(int(datetime.now().timestamp() * 1000))
        }
        
        # Add optional colors if provided
        if background_color:
            message_data['background_color'] = background_color
        if surface_color:
            message_data['surface_color'] = surface_color
        if error_color:
            message_data['error_color'] = error_color
        
        # Send theme update via FCM
        message = messaging.Message(
            data=message_data,
            topic='all_users'
        )
        
        response = messaging.send(message)
        statistics['total_theme_updates'] += 1
        statistics['fcm_success_count'] += 1
        
        # Build log with all colors
        color_list = [primary_color, secondary_color]
        if background_color: color_list.append(f'bg:{background_color}')
        if surface_color: color_list.append(f'surf:{surface_color}')
        if error_color: color_list.append(f'err:{error_color}')
        
        add_log(f'Updated theme: {" / ".join(color_list)}')
        return jsonify({'success': True, 'message_id': response})
    except Exception as e:
        statistics['fcm_failure_count'] += 1
        print(f"FCM Error: {str(e)}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/theme/reset', methods=['POST'])
@login_required
def reset_theme():
    """Reset theme colors to default via FCM"""
    try:
        # Send theme reset message with empty colors to clear SharedPreferences
        message = messaging.Message(
            data={
                'type': 'theme_reset',
                'timestamp': str(int(datetime.now().timestamp() * 1000))
            },
            topic='all_users'
        )
        
        response = messaging.send(message)
        statistics['total_theme_updates'] += 1
        statistics['fcm_success_count'] += 1
        
        add_log('Reset theme to default colors')
        return jsonify({'success': True, 'message_id': response})
    except Exception as e:
        statistics['fcm_failure_count'] += 1
        print(f"FCM Error: {str(e)}")
        return jsonify({'error': str(e)}), 500

# Feature Flags Configuration (In-memory storage)
features_config = {
    'maintenance_mode': False,
    'enable_lyrics': True,
    'enable_equalizer': True,
    'enable_downloads': True,
    'enable_sharing': True,
    'enable_playlist_creation': True
}

@app.route('/api/features/toggle', methods=['POST'])
@login_required
def toggle_feature():
    """Toggle a feature on/off via FCM"""
    try:
        data = request.get_json()
        feature_key = data.get('feature_key')
        enabled = data.get('enabled', True)
        
        if not feature_key:
            return jsonify({'error': 'Feature key required'}), 400
            
        # Update server state
        if feature_key in features_config:
            features_config[feature_key] = enabled
        
        # Send feature toggle via FCM
        message = messaging.Message(
            data={
                'type': 'feature_toggle',
                'feature_key': feature_key,
                'enabled': str(enabled).lower(),
                'timestamp': str(int(datetime.now().timestamp() * 1000))
            },
            topic='all_users'
        )
        
        response = messaging.send(message)
        statistics['total_feature_toggles'] += 1
        statistics['fcm_success_count'] += 1
        action = 'Enabled' if enabled else 'Disabled'
        
        # Special log for maintenance
        if feature_key == 'maintenance_mode':
            log_msg = f'MAINTENANCE MODE {"ACTIVATED" if enabled else "DEACTIVATED"}'
            add_log(log_msg)
        else:
            add_log(f'{action} feature: {feature_key}')
        
        return jsonify({'success': True, 'message_id': response})
    except Exception as e:
        statistics['fcm_failure_count'] += 1
        print(f"FCM Error: {str(e)}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/config')
@login_required
def get_config():
    """Get current config"""
    return jsonify({
        'features': features_config,
        'theme': {
            'primary_color': '#8B5CF6',
            'secondary_color': '#3B82F6',
            'accent_color': '#10B981'
        }
    })

@app.route('/api/logs')
@login_required
def get_logs():
    """Get activity logs from memory"""
    return jsonify(activity_logs)

@app.route('/api/stats')
@login_required
def get_stats():
    """Get dashboard statistics"""
    uptime = datetime.now() - statistics['server_start_time']
    uptime_hours = int(uptime.total_seconds() // 3600)
    uptime_minutes = int((uptime.total_seconds() % 3600) // 60)
    
    # Real User Stats from Firebase Auth
    try:
        from firebase_admin import auth
        # List all users (this might be slow for massive bases, but for < 12k it's okay-ish, 
        # ideally we paginate but for this "Total" we just count)
        # Note: list_users().iterate_all() is a generator
        all_users = [u for u in auth.list_users().iterate_all()]
        total_users = len(all_users)
        
        # Active Today (Users signed in within last 24h)
        # last_sign_in_timestamp is milliseconds since epoch
        now_ms = datetime.now().timestamp() * 1000
        one_day_ms = 24 * 60 * 60 * 1000
        active_today = sum(1 for u in all_users if u.user_metadata.last_sign_in_timestamp and (now_ms - u.user_metadata.last_sign_in_timestamp) < one_day_ms)
    except Exception as e:
        print(f"Auth Stats Error: {e}")
        total_users = 0
        active_today = 0

    # Real Server Load
    try:
        import psutil
        cpu_load = psutil.cpu_percent(interval=None)
        memory_load = psutil.virtual_memory().percent
        # Simple "Load" status logic
        if cpu_load > 80 or memory_load > 90:
            server_load = f"High ({cpu_load}%)"
        elif cpu_load > 50:
            server_load = f"Medium ({cpu_load}%)"
        else:
            server_load = f"Normal ({cpu_load}%)"
    except ImportError:
        server_load = "Normal (No psutil)"
    
    return jsonify({
        'notifications_sent': statistics['total_notifications_sent'],
        'popups_sent': statistics['total_popups_sent'],
        'theme_updates': statistics['total_theme_updates'],
        'feature_toggles': statistics['total_feature_toggles'],
        'fcm_success': statistics['fcm_success_count'],
        'fcm_failure': statistics['fcm_failure_count'],
        'total_logs': len(activity_logs),
        'uptime_hours': uptime_hours,
        'uptime_minutes': uptime_minutes,
        'last_activity': statistics['last_activity_time'].isoformat() if statistics['last_activity_time'] else None,
        
        # New Real Stats
        'total_users': total_users,
        'active_today': active_today,
        'server_load': server_load
    })

if __name__ == '__main__':
    # For development
    app.run(host='0.0.0.0', port=5000, debug=True)
    
    # For production, use gunicorn:
    # gunicorn -w 4 -b 0.0.0.0:5000 admin_server:app
