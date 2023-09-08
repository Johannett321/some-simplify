# docker-compose --profile dev up -d --build
cd ./frontend
npx tailwindcss -i ./src/index.css -o ./public/output.css --watch &
npm start
echo "Please remember to close this terminal window to stop Tailwind from watching files..."
read