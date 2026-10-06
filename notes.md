# My notes 

## How I would explain the project in 30 seconds
It's a task manager where teams work in workspaces. Managers create projects and
assign tasks, members move tasks across a board. My own addition is a workload
score that warns a manager before they overload someone.

## Login and security
- Passwords are hashed with BCrypt. Even I can't read them in the database.
- Login gives back a JWT. The React app sends it with every request.
- A filter on the server reads the token and finds out who is calling.
- Why JWT: the server doesn't need to remember sessions, so it's simple to run.

## Roles and permissions
- Role is stored in the workspace membership table, not on the user.
- Only a MANAGER can create projects, add members, assign and delete tasks.
- I check this on the backend. I tested it in Postman: the second user got 403.

## Tasks
- Any member can create and edit a task. Only a manager can delete one.
- The `version` field is optimistic locking. If two people save the same task
  at the same time, the second save fails instead of overwriting.
- Deleting a task removes its comments first, inside one transaction, so it is
  all or nothing.

## My unique feature (workload)
- Problem: tools let you assign tasks to someone who is already overloaded.
- My rule: points for priority, extra points for deadlines close or missed.
- It warns (409) instead of blocking, so the manager still decides.
- Trade-off: I calculate it on every request. That is fine for a small team.
  For a big one I would cache it.
- If I had more time: hours per task, leave days, and a notification when
  someone becomes overloaded.

## Dashboard
- Manager only. Counts by status, overdue tasks, open tasks per person.

## A problem I solved
- My "invalid status" request showed 403 instead of 400. Spring sends errors
  through an internal /error page and my security rules were blocking it.
  Allowing /error fixed it.

## What I would improve
- Email notifications, pagination for big lists, automated tests, AWS deploy.