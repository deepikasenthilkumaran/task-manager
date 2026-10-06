# TaskFlow - Task and Project Management

Hi, I'm Deepika. This is my take on a simple Trello/Jira-style app. I built it
step by step, and I kept the code in this repo so you can see how it grew.

**Stack:** Spring Boot (Java), React, PostgreSQL, JWT, Git

## What you can do in the app
- Register and log in (passwords are stored hashed, never as plain text)
- Create workspaces and projects
- Add team members as MANAGER or MEMBER
- Create, edit, delete and assign tasks
- Set status, priority, due date and labels
- Drag a task card between To Do, In Progress and Completed
- Comment on tasks
- Get an in-app notification when a task is assigned to you
- Managers get a dashboard with task counts and overdue tasks

## The idea I added myself: workload-aware assignment
In most tools you can give a task to anyone, even a person who is already
buried in work. I wanted to fix that.

Every open task gives its owner some points. A higher priority gives more
points, and a close or missed deadline adds extra points. Then:
- under 5 points: FREE
- 5 to 9 points: BALANCED
- 10 or more: OVERLOADED

If a manager tries to assign a task to an overloaded person, the app warns them
first. They can still go ahead if they really want to, because sometimes there
is a good reason. There is also a "Suggest assignee" button that points to the
person with the lightest load.

## A few choices I made
- **Roles are per workspace.** Someone can be a manager in one team and a
  normal member in another, so I stored the role with the membership.
- **Permissions are checked on the server.** Hiding a button is not security,
  because anyone can call the API directly.
- **Optimistic locking.** Tasks have a version number, so two people editing
  the same task can't silently overwrite each other.

## What is not done yet
- Email notifications (only in-app notifications for now)
- Deployment to AWS (it runs locally)

## How to run it
1. Install Java 17, Node.js and PostgreSQL.
2. Create a database called `taskmanager_db`.
3. Put your own database password in
   `backend/src/main/resources/application.properties`.
4. Run `BackendApplication` (it starts on port 8080).
5. In the `frontend` folder, run `npm install` and then `npm run dev`.
6. Open http://localhost:5173