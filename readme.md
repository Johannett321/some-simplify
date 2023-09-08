# Spring Angular Starter Template

## About
This is a template for the Spring & Angular stack. Here is the complete list
#### Frontend
- ReactJS
- Typescript
- TailwindCSS

#### Backend
- Spring Boot
- Spring Security
- Spring Data JPA
- Docker-compose

### Installation
This is a guide on how to install and configure the system on a new machine. <b><u>THIS SHOULD ONLY BE DONE ONCE ON EACH MACHINE</u></b>. Make sure you have the necessary requirements before proceeding with the cloning.

#### Requirements
Make sure you have the following requirements:
- Java 20
- Docker
- Git
- Maven
- Node and npm (Node Package Manager)

#### Cloning the application
Start by cloning the application inside your desired location
```
git clone <project_url>
```

#### Installing frontend packages
Install all frontend packages by running the following command inside the frontend folder:
```
npm install
```

#### Installing backend packages
Install all backend packages by running the following command inside the backend folder:
```
mvn clean install
```

#### Update the following files
Update the name of the project and some other things in the following places:
- /frontend/package.json (around line 2)
- docker-compose.yml (around line 9 and 22)
- /backend/pom.xml (around line 11, 14 and 15)
- /backend/src/main/java/com/johansvartdal/ReactSpringTemplate (refactor the filename and the package name)

#### Update the Clerk Key
Insert the Clerk API key in the "/frontend/.env" file

## Starting the application
Run the following command in the root directory of the project
```
./start-dev.sh
```
### Accessing the applicaiton
#### Frontend
The frontend application will now be accessible at
```
localhost:3000
```

#### Backend
Backend requests can be sent to:
```
localhost:8080
```

#### Database
The database is not available outside docker, but can be accessed on this adress inside docker:
```
localhost:3306
```