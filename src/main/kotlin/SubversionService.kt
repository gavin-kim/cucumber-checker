import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.auth.SVNSSLAuthentication
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory
import org.tmatesoft.svn.core.internal.wc.DefaultSVNSSLTrustManager
import org.tmatesoft.svn.core.internal.wc.ISVNConnectionOptions
import org.tmatesoft.svn.core.internal.wc17.SVNWCUtils
import org.tmatesoft.svn.core.io.ISVNSession
import org.tmatesoft.svn.core.io.SVNRepositoryFactory
import org.tmatesoft.svn.core.wc.ISVNOptions
import org.tmatesoft.svn.core.wc.SVNClientManager
import org.tmatesoft.svn.core.wc.SVNWCUtil
import java.io.File

class SubversionService {

}

fun main(args: Array<String>) {
    val url = "/wfm/WORKBRAIN/Source/projects/BranchesToBeIndexed-January2019/EricWang/WFM-21168-Shared"

    DAVRepositoryFactory.setup()
    val svnUrl = SVNURL.parseURIEncoded("https://wfmsvn.infor.com/svn$url")

    val svnRepository = SVNRepositoryFactory.create(svnUrl)
    val authManager = SVNWCUtil.createDefaultAuthenticationManager("ykim6", "rlaDbsrhks74*$".toCharArray())

    svnRepository.authenticationManager = authManager
    println(svnRepository.latestRevision)

/*
    val options = SVNWCUtil.createDefaultAuthenticationManager()
    val svnClientManager = SVNClientManager.newInstance(null, "ykim6", "rlaDbsrhks74*$")*/

}