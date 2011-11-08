#
# Test data for Tori.
# SQL clauses cannot span over multiple lines.
#
# Categories
insert into CATEGORY set ID = 1, NAME = "News & Announcements", DESCRIPTION = "All news and announcements.";
insert into CATEGORY set ID = 2, NAME = "Using the Forums", DESCRIPTION = "Guidelines and tips for using the forums.";
insert into CATEGORY set ID = 3, NAME = "Vaadin Discussion", DESCRIPTION = "All Vaadin related discussion.";
insert into CATEGORY set ID = 4, NAME = "Git Discussion", DESCRIPTION = "All Git related discussion.";

# SubCategories
insert into CATEGORY set ID = 5, PARENTCATEGORY_ID = 1, NAME = "News", DESCRIPTION = "All news.";
insert into CATEGORY set ID = 6, PARENTCATEGORY_ID = 1, NAME = "Announcements", DESCRIPTION = "All announcements.";

# Threads
insert into THREAD set ID = 1, TOPIC = "Tori alpha 1 released";
insert into THREAD set ID = 2, TOPIC = "Writing new messages";
insert into THREAD set ID = 3, TOPIC = "Issues with widgetset compilation";
insert into THREAD set ID = 4, TOPIC = "What does 'git rebase' actually do";

## Users
insert into `USER` set ID = 1, DISPLAYEDNAME = "John Doe";
insert into `USER` set ID = 2, DISPLAYEDNAME = "Molly Townsend";

## Posts
insert into POST set ID = 1, `TIME` = "2011-10-1 12:00:00", THREAD_ID = 1, AUTHOR_ID = 1;
insert into POST set ID = 2, `TIME` = "2011-10-2 12:00:00", THREAD_ID = 1, AUTHOR_ID = 2;
insert into POST set ID = 3, `TIME` = "2011-10-1 12:00:00", THREAD_ID = 2, AUTHOR_ID = 1;
insert into POST set ID = 4, `TIME` = "2011-10-2 12:00:00", THREAD_ID = 2, AUTHOR_ID = 2;
insert into POST set ID = 5, `TIME` = "2011-10-1 12:00:00", THREAD_ID = 3, AUTHOR_ID = 1;
insert into POST set ID = 6, `TIME` = "2011-10-2 12:00:00", THREAD_ID = 3, AUTHOR_ID = 2;
insert into POST set ID = 7, `TIME` = "2011-10-1 12:00:00", THREAD_ID = 4, AUTHOR_ID = 1;
insert into POST set ID = 8, `TIME` = "2011-10-2 12:00:00", THREAD_ID = 4, AUTHOR_ID = 2;
