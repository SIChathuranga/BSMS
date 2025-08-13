#!/usr/bin/env python3
import http.server
import socketserver
import os
import sys

# Change to the directory containing this script
os.chdir(os.path.dirname(os.path.abspath(__file__)))

PORT = 3000

class MyHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def end_headers(self):
        # Add CORS headers
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization')
        super().end_headers()

    def do_OPTIONS(self):
        self.send_response(200)
        self.end_headers()

if __name__ == "__main__":
    print(f"🚀 Starting BSMS Frontend Server...")
    print(f"📂 Serving directory: {os.getcwd()}")
    print(f"🌐 Server URL: http://localhost:{PORT}")
    print(f"🏠 Main App: http://localhost:{PORT}/index.html")
    print(f"📖 Setup Guide: http://localhost:{PORT}/modern-setup-guide.html")
    print(f"⚙️  Admin Panel: http://localhost:{PORT}/admin/")
    print(f"\n✨ Features:")
    print(f"   • Modern responsive design")
    print(f"   • Hero carousel with motorbike images")
    print(f"   • Real-time search and filtering")
    print(f"   • Smooth animations and transitions")
    print(f"   • Firebase authentication")
    print(f"   • Shopping cart functionality")
    print(f"\n📝 Note: Make sure backend is running on http://localhost:8081")
    print(f"\n🛑 Press Ctrl+C to stop the server")
    print("-" * 60)

    with socketserver.TCPServer(("", PORT), MyHTTPRequestHandler) as httpd:
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print(f"\n\n🛑 Server stopped.")
            sys.exit(0)
