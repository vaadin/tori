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
insert into DISCUSSIONTHREAD set ID = 1, CATEGORY_ID = 5, TOPIC = "Tori alpha 1 released";
insert into DISCUSSIONTHREAD set ID = 2, CATEGORY_ID = 2, TOPIC = "Writing new messages";
insert into DISCUSSIONTHREAD set ID = 3, CATEGORY_ID = 3, TOPIC = "Issues with widgetset compilation";
insert into DISCUSSIONTHREAD set ID = 4, CATEGORY_ID = 4, TOPIC = "What does 'git rebase' actually do";
insert into DISCUSSIONTHREAD set ID = 5, CATEGORY_ID = 6, TOPIC = "Announced the future release date of Tori alpha 2";
insert into DISCUSSIONTHREAD set ID = 6, CATEGORY_ID = 5, TOPIC = "Something new is happening";
insert into DISCUSSIONTHREAD set ID = 7, CATEGORY_ID = 3, TOPIC = "How to deploy Vaadin applications to Liferay";
insert into DISCUSSIONTHREAD set ID = 8, CATEGORY_ID = 3, TOPIC = "How to change the color of a Label";

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
insert into POST set ID = 9, `TIME` = "2011-10-4 12:00:00", THREAD_ID = 5, AUTHOR_ID = 2;
insert into POST set ID = 10, `TIME` = "2011-10-4 12:00:00", THREAD_ID = 6, AUTHOR_ID = 2;
insert into POST set ID = 11, `TIME` = "2011-10-4 12:00:00", THREAD_ID = 7, AUTHOR_ID = 2;
insert into POST set ID = 12, `TIME` = "2011-10-5 12:00:00", THREAD_ID = 8, AUTHOR_ID = 2;
