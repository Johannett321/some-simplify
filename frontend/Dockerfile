# Use the official Node.js image to build our React app
FROM node:18 AS build

# Set the working directory
WORKDIR /usr/src/app

# Copy the package.json and package-lock.json
COPY ./package*.json ./

# Install the app dependencies
RUN npm install

# Copy the rest of the application files
COPY . .

# Build the React app
RUN npm run build

# Serve the React app using a lightweight node server, for example serve
FROM node:18-slim

# Install serve
RUN npm install -g serve

# Copy the build folder from the build stage
COPY --from=build /usr/src/app/build /app/build

# Command to run the app
CMD ["serve", "-s", "app/build", "-l", "3000"]