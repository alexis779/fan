#include <stdio.h>
#include <errno.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/stat.h>
#include <ev.h>
#include <string.h>

# define LOG stderr

// the object passed to the event callback
typedef struct _Info {
	FILE* input;
} Info;

// the event listener
static void fifo_callback(EV_P_ struct ev_io *w, int revents);

static int create_fifo(char* fifo_name);

/*
   Create an infinite loop that is listening to read IO events on a file.
*/
static struct ev_loop* create_loop(char* fifo_name) {
	struct ev_loop *loop;
	struct ev_io *event;
	FILE* file;
	Info *info;
	int sockfd;

	sockfd = create_fifo(fifo_name);
	file = malloc(sizeof(FILE));
	file = fdopen(sockfd, "r");
	
	event = malloc(sizeof(struct ev_io));
	ev_io_init(event, (void *) &fifo_callback, sockfd, EV_READ);

	info = malloc(sizeof(Info));
	info->input = file;
	event->data = info;
	
	loop = malloc(sizeof(ev_loop));
	loop = ev_default_loop(0);
	ev_io_start(loop, event);

	return loop;
}

/*
   Return a file descriptor corresponding to a named pipe.
   The file "modes" are non blocking and overwrite.
*/
static int create_fifo(char* fifo_name) {
	struct stat st;
	int sockfd;

	if (lstat (fifo_name, &st) == 0) { 
		if ((st.st_mode & S_IFMT) == S_IFREG) {
			errno = EEXIST;
			perror("lstat");
			exit(1);
		}
	}
	unlink(fifo_name);
	if (mkfifo(fifo_name, 0600) == -1) {
		perror("mkfifo");
		exit (1);
	}
	sockfd = open(fifo_name, O_RDWR | O_NONBLOCK, 0);
	if (sockfd == -1) {
		perror("open");
		exit(1);
	}
	return sockfd;
}

/* This gets called whenever data is received from the fifo */
static void fifo_callback(EV_P_ struct ev_io *w, int revents) {
	char s[1024];
	long int rv = 0;
	int n = 0;
	Info *info = (Info*) w->data;

	do {
		s[0] = '\0';
		// read the first 1023 characters and store the number of characters read into integer n.
		rv = fscanf(info->input, "%1023s%n", s, &n);
		s[n] = '\0'; 
		if (n && s[0]) {
			fprintf(LOG, "Read %d characters: %s\n", n, s);
		} else {
			break;
		}
	} while ( rv != EOF );
}


int main() {
	struct ev_loop *loop;
	char* fifo_name = "my.fifo";

	fprintf(LOG, "Creating named pipe \"%s\"\n", fifo_name);
	loop = create_loop(fifo_name);

	fprintf(LOG, "Now, write data into the fifo > %s\n", fifo_name);

	ev_loop(loop, 0);

	return 0;
}
