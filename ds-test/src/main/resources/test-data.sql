#
# Test data for Tori.
# SQL clauses cannot span over multiple lines.
#
# Categories
insert into CATEGORY set ID = 1, NAME = "News & Announcements", DESCRIPTION = "All news and announcements.";
insert into CATEGORY set ID = 2, NAME = "Using the Forums", DESCRIPTION = "Guidelines and tips for using the forums.";
insert into CATEGORY set ID = 3, NAME = "Vaadin Discussion", DESCRIPTION = "All Vaadin related discussion.";
insert into CATEGORY set ID = 4, NAME = "Git Discussion", DESCRIPTION = "All Git related discussion.";

# Threads
insert into THREAD set ID = 1, TOPIC = "Tori alpha 1 released";
insert into THREAD set ID = 2, TOPIC = "Writing new messages";
insert into THREAD set ID = 3, TOPIC = "Issues with widgetset compilation";
insert into THREAD set ID = 4, TOPIC = "What does 'git rebase' actually do";
