package com.hashim.instagram.data.repository

import com.hashim.instagram.data.local.db.dao.PostDao
import com.hashim.instagram.data.model.Post
import com.hashim.instagram.data.model.User
import com.hashim.instagram.data.remote.NetworkService
import com.hashim.instagram.data.remote.request.PostCreationRequest
import com.hashim.instagram.data.remote.request.PostLikeModifyRequest
import com.hashim.instagram.data.remote.response.GeneralResponse
import com.hashim.instagram.utils.log.Logger
import com.hashim.instagram.utils.network.NetworkBoundResource
import com.hashim.instagram.utils.network.NetworkHelper
import io.reactivex.Single
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val networkService: NetworkService,
    private val networkHelper: NetworkHelper,
    private val postDao: PostDao
) {

    companion object{
        val TAG = "PostRepository"
    }

    fun fetchHomePostList(firstPostId: String?, lastPostId: String?, user: User): Single<List<Post>> {
//      return networkService.doHomePostListCall(
//            firstPostId,
//            lastPostId,
//            user.id,
//            user.accessToken
//        ).map { it.data }
//
//          val hasNetworkConnection = networkHelper.isNetworkConnected()
//        var observableFromApi: Single<List<Post>>? = null
//        if (hasNetworkConnection){
//            observableFromApi = fetchHomePostListFromApi(firstPostId,lastPostId,user)
//        }
//        val observableFromDb = fetchHomePostListFromDB()
//
//        return if (hasNetworkConnection) observableFromApi!! else observableFromDb!!

        return object : NetworkBoundResource(){
            override fun saveCallResult(request: List<Post>) {
                postDao.preparePostAndCreator(request)
            }

            override fun loadFromDb(): Single<List<Post>> {
                return postDao.getAll().map {
                    val arrayList = mutableListOf<Post>()
                    for (item in it) {
                        for (items in item.postEntity) {
                            arrayList.add(
                                Post(
                                    items.id,
                                    items.imageUrl,
                                    items.imageWidth,
                                    items.imageHeight,
                                    Post.User(
                                        item.userEntity.id,
                                        item.userEntity.name,
                                        item.userEntity.profilePicUrl
                                    ),
                                    mutableListOf(),
                                    items.createdAt
                                )
                            )
                            Logger.d(TAG, items.toString())
                        }
                    }
                    arrayList

                }
            }

            override fun createCall(): Single<List<Post>> {
                return networkService.doHomePostListCall(
                    firstPostId,
                    lastPostId,
                    user.id,
                    user.accessToken
                ).map {
                    it.data
                }
            }

            override fun shouldFetch(): Boolean {
                return networkHelper.isNetworkConnected()
            }

        }.asSingle()

    }

    fun makeLikePost(post: Post, user: User): Single<Post> {
        return networkService.doPostLikeCall(
            PostLikeModifyRequest(post.id),
            user.id,
            user.accessToken
        ).map {
            post.likedBy?.apply {
                this.find { postUser -> postUser.id == user.id } ?: this.add(
                    Post.User(
                        user.id,
                        user.name,
                        user.profilePicUrl
                    )
                )
            }
            return@map post
        }
    }

    fun makeUnlikePost(post: Post, user: User): Single<Post> {
        return networkService.doPostUnlikeCall(
            PostLikeModifyRequest(post.id),
            user.id,
            user.accessToken
        ).map {
            post.likedBy?.apply {
                this.find { postUser -> postUser.id == user.id }?.let { this.remove(it) }
            }
            return@map post
        }
    }

    fun createPost(imgUrl : String, imgWidth : Int, imgHeight : Int, user: User) : Single<Post>{
        return networkService.doCreatePost(PostCreationRequest(imgUrl,imgWidth, imgHeight),user.id,user.accessToken).map {
            Post(
                it.data.id,
                it.data.imageUrl,
                it.data.imageWidth,
                it.data.imageHeight,
                Post.User(
                    user.id,
                    user.name,
                    user.profilePicUrl
                ),
                mutableListOf(),
                it.data.createdAt
            )
        }

    }


    fun fetchUserPostList(user: User) : Single<List<Post>> =
        networkService.doMyPostsCall(user.id,user.accessToken).map { it.data }

    fun deleteUserPost(postId: String, user: User) : Single<GeneralResponse> =
        networkService.doPostDelete(postId,user.id,user.accessToken)


//    private fun fetchHomePostListFromApi(firstPostId: String?, lastPostId: String?, user: User): Single<List<Post>> {
//        return networkService.doHomePostListCall(
//            firstPostId,
//            lastPostId,
//            user.id,
//            user.accessToken
//        ).map {
//            postDao.preparePostAndCreator(it.data)
//            it.data
//        }
//
//    }
//
//    private fun fetchHomePostListFromDB() : Single<List<Post>>?{
//        return postDao.getAll().map {
//            val arrayList = mutableListOf<Post>()
//            for (item in it) {
//                for (items in item.postEntity) {
//                    arrayList.add(
//                        Post(
//                            items.id,
//                            items.imageUrl,
//                            items.imageWidth,
//                            items.imageHeight,
//                            Post.User(
//                                item.userEntity.id,
//                                item.userEntity.name,
//                                item.userEntity.profilePicUrl
//                            ),
//                            mutableListOf(),
//                            items.createdAt
//                        )
//                    )
//                    Logger.d(TAG, items.toString())
//                }
//            }
//            write(arrayList)
//            arrayList
//
//        }
//    }
//
//    private fun processLikes(list: MutableList<LikedUserEntity>?) : MutableList<Post.User>?{
//        var mutableList = mutableListOf<Post.User>()
//        mutableList.clear()
//        for (item in list!!){
//            mutableList.add(Post.User(item.id,item.name,item.profilePicUrl))
//        }
//
//        return mutableList
//    }
//
//    private fun write(mutableList : MutableList<Post>){
//        for(item in mutableList){
//            Logger.d(TAG,item.toString())
//        }
//    }








}