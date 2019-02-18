//import java.io.IOException;
import java.util.Iterator;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.GetBucketLocationRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

import java.io.File;

// add other aws imports

/**
 *
 * @author kunwadee
 */
public class MyS3SDKDemo5831034621 {

    /**
     * @param args the command line arguments
     */

    public static void main(String[] args) {

        /* This is the basic way.  But your source code will contain your key.
        Secure?  No way!    So, we won't use this.

        BasicAWSCredentials awsCreds = new BasicAWSCredentials("__yourAccessKeyId__", "__yourSecretAccessKey__");
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
        */
        String clientRegion = "ap-southeast-1";

        AmazonS3 s3Client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new ProfileCredentialsProvider())
                .withRegion(clientRegion)
                .build();

        String bucketName = "mydropbox123b";
        createBucket(s3Client, bucketName);
        viewObjectsInBucket(s3Client, bucketName);
        addObjectToBucket (s3Client, bucketName, "key", "/Users/burin/Desktop/test_img.png");
        viewObjectsInBucket(s3Client, bucketName);
        deleteBucket(s3Client,bucketName);

    }

    // Create bucket named bucketName if it does not yet exist.
    // Catch all exceptions, and print error to stdout (System.out)
    private static void createBucket (AmazonS3 s3Client, String bucketName) {

        try {
            // Check if bucket exists, and if does not exist create new bucket named bucketName in S3
//        	if (!s3Client.doesBucketExistV2(bucketName)) {
            // Because the CreateBucketRequest object doesn't specify a region, the
            // bucket is created in the region specified in the client.
            s3Client.createBucket(new CreateBucketRequest(bucketName));

            // Verify that the bucket was created by retrieving it and checking its location.
            String bucketLocation = s3Client.getBucketLocation(new GetBucketLocationRequest(bucketName));
            System.out.println("Bucket location: " + bucketLocation);
//            }

        } catch (AmazonServiceException ase) {
            // Study Exceptions thrown, decode and print error to stdout
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        }
        catch(SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }
    }

    // Add the object in filePath on my computer to the bucketName bucket on S3, using the key keyName for the object
    // Catch all exceptions, and print error to stdout (System.out)
    public static void addObjectToBucket(AmazonS3 s3Client, String bucketName, String keyName, String filePath) {

        TransferManager tm = TransferManagerBuilder
                .standard()
                .withS3Client(s3Client)
                .withMultipartUploadThreshold((long) (5 * 1024 * 1025))
                .build();
        // Use TransferManager to upload file to S3
        try {

            // Block and wait for the upload to finish

            Upload upload = tm.upload(bucketName, keyName, new File(filePath));
            System.out.println("Object upload started");
            // Optionally, wait for the upload to finish before continuing.
            upload.waitForCompletion();
            System.out.println("Object upload complete");

        } catch (AmazonClientException | InterruptedException E) {
            // Study Exceptions thrown, decode and print error to stdout
            E.printStackTrace();
        }
        // so this call returns immediately.
    }

    // List all objects in the bucketName bucket
    // Catch all exceptions, and print error to stdout (System.out)
    public static void viewObjectsInBucket(AmazonS3 s3Client, String bucketName) {
        System.out.println("list objects...");
        try {
            ObjectListing objectListing = s3Client.listObjects(new ListObjectsRequest()
                    .withBucketName(bucketName));
            while(true) {
                for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    //                 System.out.println(" - " + objectSummary.getKey() + "  " +
                    //                                    "(size = " + objectSummary.getSize() + ")");
                    System.out.println(objectSummary.toString());
                }
                if(objectListing.isTruncated())
                    objectListing = s3Client.listNextBatchOfObjects(objectListing);
                else break;
            }
        }
        catch (AmazonServiceException ase) {
            // Study Exceptions thrown, decode and print error to stdout
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        }
    }

    // Delete all objects and versions in the bucketName bucket, and then
    // delete the bucket itself.
    // Catch all exceptions, and print error to stdout (System.out)
    public static void deleteBucket(AmazonS3 s3Client, String bucketName) {

        try {
            ObjectListing objectListing = s3Client.listObjects(bucketName);
            while (true) {
                Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();
                while (objIter.hasNext()) {
                    s3Client.deleteObject(bucketName, objIter.next().getKey());
                }
                // If the bucket contains many objects, the listObjects() call
                // might not return all of the objects in the first listing. Check to
                // see whether the listing was truncated. If so, retrieve the next page of objects
                // and delete them.
                if (objectListing.isTruncated()) {
                    objectListing = s3Client.listNextBatchOfObjects(objectListing);
                } else {
                    break;
                }
            }
            s3Client.deleteBucket(bucketName);
            System.out.println(bucketName + " is deleted.");
        } catch (Exception E) {
            E.printStackTrace();
        }


    }

}



