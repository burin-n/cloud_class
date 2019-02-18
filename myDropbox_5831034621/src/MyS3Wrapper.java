import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

import java.io.*;
import java.util.Iterator;


public class MyS3Wrapper {
        // Create bucket named bucketName if it does not yet exist.
    // Catch all exceptions, and print error to stdout (System.out)

    private AmazonS3 s3Client;
    Debugger debugger = myDropbox_5831034621.debugger;

    public MyS3Wrapper(String clientRegion) {

        this.s3Client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new ProfileCredentialsProvider())
                .withRegion(clientRegion)
                .build();
    }

    public void createBucket(String bucketName) {
        try {
            // Check if bucket exists, and if does not exist create new bucket named bucketName in S3
        	if (!s3Client.doesBucketExistV2(bucketName)) {
                // Because the CreateBucketRequest object doesn't specify a region, the
                // bucket is created in the region specified in the client.
                s3Client.createBucket(new CreateBucketRequest(bucketName));

                // Verify that the bucket was created by retrieving it and checking its location.
                String bucketLocation = s3Client.getBucketLocation(new GetBucketLocationRequest(bucketName));
                debugger.log("Bucket location: " + bucketLocation);
            }

        } catch (AmazonServiceException ase) {
            printTrace(ase);
        }
        catch(SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }
        debugger.log("bucket is Created");

    }

    // Add the object in filePath on my computer to the bucketName bucket on S3, using the key keyName for the object
    // Catch all exceptions, and print error to stdout (System.out)
    public boolean addObjectToBucket(String bucketName, String keyName, String filePath) {

        TransferManager tm = TransferManagerBuilder
                .standard()
                .withS3Client(s3Client)
                .withMultipartUploadThreshold((long) (5 * 1024 * 1025))
                .build();
        // Use TransferManager to upload file to S3
        try {

            // Block and wait for the upload to finish

            Upload upload = tm.upload(bucketName, keyName, new File(filePath));
            System.out.println(keyName + " is being upload...");
            // Optionally, wait for the upload to finish before continuing.
            upload.waitForCompletion();
            System.out.println("upload complete");
            return true;

        } catch (AmazonClientException | InterruptedException E) {
            // Study Exceptions thrown, decode and print error to stdout
            debugger.log("s3 put exception");
            E.printStackTrace();
            return false;
        }
        // so this call returns immediately.
    }


    // List all objects in the bucketName bucket
    // Catch all exceptions, and print error to stdout (System.out)
    public void viewObjectsInBucket(String bucketName) {
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
            printTrace(ase);
        }
    }

    // Delete all objects and versions in the bucketName bucket, and then
    // delete the bucket itself.
    // Catch all exceptions, and print error to stdout (System.out)
    public void deleteBucket(String bucketName) {

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
            E.getMessage();
        }
    }

    public void getObjectMeta(String bucketName, String key){
        ObjectMetadata meta = s3Client.getObjectMetadata(bucketName, key);
        System.out.print(key + " ");
        System.out.print(meta.getContentLength() + " ");
        System.out.print(meta.getLastModified() + " ");
        System.out.println(bucketName.split("-")[1]);

    }

    public void downloadObject(String bucketName, String key){
        try {

            File localFile = new File(bucketName + "_" + key);
//            S3Object object = s3Client.getObject(bucketName, key);
//            InputStream reader = new BufferedInputStream(
//                    object.getObjectContent());
//
//            OutputStream writer = new BufferedOutputStream(new FileOutputStream(localFile));
//
//            int read = -1;
//            while ( ( read = reader.read() ) != -1 ) {
//                writer.write(read);
//            }
//
//            writer.flush();
//            writer.close();
//            reader.close();

            s3Client.getObject(new GetObjectRequest(bucketName, key), localFile);
        }catch(Exception E){
            System.out.println(E.getMessage());
        }

    }

    private void printTrace(AmazonServiceException ase){
        // Study Exceptions thrown, decode and print error to stdout
        debugger.log("Caught an AmazonServiceException, which means your request made it "
                + "to Amazon S3, but was rejected with an error response for some reason.");
        debugger.log("Error Message:    " + ase.getMessage());
        debugger.log("HTTP Status Code: " + ase.getStatusCode());
        debugger.log("AWS Error Code:   " + ase.getErrorCode());
        debugger.log("Error Type:       " + ase.getErrorType());
        debugger.log("Request ID:       " + ase.getRequestId());
    }

}
