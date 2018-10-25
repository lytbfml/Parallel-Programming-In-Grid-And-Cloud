#include <iostream>  // cout
#include <strings.h> // bzero
#include <unistd.h>  // gethostname
#include "mpi.h"
#define BUFSIZE 100

using namespace std;

int main( int argc, char* argv[] ) {
  int my_rank;              // rank of process
  int size;                 // #processes
  int tag = 0;              // tag for messages
  char message[BUFSIZE];    // return status for 
  MPI_Status status;        // receive
  char hostname[BUFSIZE];   // host name

  MPI_Init( &argc, &argv ); // Start up MPI

  MPI_Comm_rank( MPI_COMM_WORLD, &my_rank ); // Find out process rank
  MPI_Comm_size( MPI_COMM_WORLD, &size );    // Find out # processes
  bzero( hostname, BUFSIZE );
  gethostname( hostname, BUFSIZE );          // Find out IP name

  if ( my_rank != 0 ) {     // slaves
    bzero( message, BUFSIZE );
    sprintf( message, "rank = %i hostname = %s size = %i", my_rank, hostname, size );
    int dest = 0;
    MPI_Send( message, BUFSIZE, MPI_CHAR, dest, tag, MPI_COMM_WORLD );
  } else { // my_rank == 0, i.e., the master
    for ( int source = 1; source < size; source++ ) {
      MPI_Recv( message, BUFSIZE, MPI_CHAR, source, tag, MPI_COMM_WORLD, &status );
      cout << message << endl;
    }
    printf( "rank = %i hostname = %s size = %i\n", my_rank, hostname, size );
  }

  MPI_Finalize( );          // Shut down MPI
}
