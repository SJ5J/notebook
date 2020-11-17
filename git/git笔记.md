# 1. 版本控制
- 版本控制的需求：修改了一个或多个文件，需要撤回修改。

# 2. git与svn的区别
- 文件存储
    - svn：只存储文件修改的增量信息。当代码修改越来越多，速度会越来越慢。
    - git：对于修改的文件保留其快照（整个文件？），对于未修改的文件保留其索引(HASH)，用空间换时间。
- git是分布式版本工具。svn是中央版本控制工具。

# 3. 项目使用git流程
1. A将代码先提交到A自己本地仓库
2. 再从本地仓库提交到远程仓库
3. B会从远程仓库下载代码，然后修改
4. 再提交到B自己的本地仓库
5. 再由本地仓库提交到远程仓库
6. 项目经理从远程仓库下载到本地，并核实A和B的修改，没有问题就合并代码。

# 4. git本地仓库：三个区
1. 工作区：写代码，修改代码
2. 暂存区（index / staging area）：缓存 cache stage
3. （本地）版本库：
4. 流程：**working directory** --git add --> **staging area**  -- git commit--> **repository**
    1. 用git add 将代码由工作区提交到暂存区
    2. 确定无误后，再从暂存区提交到版本库：git commit;

# 5. git远程仓库（如github.com）
1. 团队内部成员可以从远程仓库下载代码（pull、clone），或向远程仓库上传代码（push）;
2. 团队的外部人员，则需要先从远程仓库 fork 代码到自己的远程仓库;
3. 然后从自己的远程仓库下载到本地;
4. 修改后 commit 到自己的本地仓库;
5. 再从本地库上传 push 到自己的远程仓库;
6. 再向那个团队的远程仓库提交 pull request
7. 团队成员将 pull request 的远程仓库代码下载到自己本地仓库;
8. 确认没问题后，将修改的代码合并 merge 到团队的远程仓库;

# 命令
```
git clone
git pull / git push
git commit
git add

git branch 
git checkout /git chekcout -b
git merge
git rebase
git remote
git reset
git revert
git reflog / git log

git format-patch
git apply
git pick-cherry

```
# 6. 操作流程
1. mkdir test && cd test && git init
2.  配置仓库(提交者信息：git帐号的用户名和邮箱)
    - git config --global user.name "SJ5J"
    - git config --global user.email "song-jian1234@163.com"
    - git config --list
    - 配置范围：
        - --system 针对所有用户
        - --global 针对当前用户
        - 缺省，当前项目
        - 级别： 当前项目 > global > system
3. 生成密钥，并把公钥配到github上
    - ssh-keygen -t rsa -C "song-jian1234@163.com"  (需要输入 /root/.ssh/id_rsa_github，然后两次回车)
    - cat /root/.ssh/id_rsa_github.pub
    - 复制公钥，粘帖到github的头像-> settings-> SSH and GPG keys->右上角New SSH key
    - ssh -T git@github.com  (测试)
4. 在github页面上创建远程仓库: firstrepo
5. echo "# first git test" >> README.md
6. git status （查看当前版本状态，提示 git add）
7. git add README.md  
8. git status （提示此时撤销用 git rm --cached README.md）
9. git commit -m "first commit"
10. git log  (查看版本提交记录: commitID和作者等信息)
11. 添加远程仓库的地址(origin 是远程仓库的别名，用来代替那一长串)
    - git remote add origin git@github.com:****/firstrepo
12. git remote -v    查看
13. 向远程仓库默认的master分支提交代码
    - git push -u origin master

# 7. 版本撤销
- git log --pretty=oneline
- git log --oneline
- git log --graph
- git reset commitID --参数
    - 如果当前有4个版本a-> b -> c -> d，要从d回退到c。三个参数分别代表回退后三个区（版本库，暂存区，工作区）的三种状态。三个区都是回退到c，区别在是否保留d版本：
    - --hard ：暂存区删掉d版本，工作区删掉d版本
    - --soft ：暂存区保留d版本，工作区保留d版本
    - --mixed（默认）： 暂存区删除d版本，工作区保留d版本
    - 用git status和git log 查看比较这三个参数的状态

# 8. git diff（比较三个区）
- git diff： 暂存区（旧）vs 工作区（新）
- git diff --cached：版本库（当前版本）  vs 暂存区
- git diff HEAD：版本库（当前版本） vs 工作区
- diff信息：
    - a/a b/a：a是比较前的版本，b是比较后的版本
    - index 文件哈希值
    - ---a/a 代表变动前的版本，+++b/a代表变动后的版本
    - @@ -1 +1,2 @@：- + 代表变动前后，1,2代表行号
    - ab没变化
    - +ac 表示改变后多了ac

# 9. git log & git reflog
- git log 看不到被git reset撤销的commitID，git reflog可以

# 10. git HEAD解析
- git init后生成 .git文件夹
- .git/HEAT 文件的内容又指向了 .git/refs/heads/master 
- 而master 文件的内容则是 commitID
- git branch dev 创建一个分支dev，则生成文件.git/refs/heads/dev， 内容也是commitID
- git checkout dev 切换到dev分支
- git status 查看，当前处于dev分支
- 查看 .git/HEAT 文件内容，则指向.git/refs/heads/dev
- 所以HEAD表示当前处于哪个分支上。而不同分支有各自的提交（commitID）。

# 11. git reset 和git revert
- 当前历史版本为 a -> b -> c ;
- git revert b 后，则b版本被删除了，a和c版本保留了，并做了新的提交（删除b）。
- 即 git revert 与 git reset结果相反，revert用来删除版本，reset用来回退版本。

# 12. git合并分支并解决合并出现的冲突
- git branch -v  查看分支
- git branch feature  
- git checkout master 切换到master分支，且master与feature的某个文件内容不同
- git merge feature   将feature分支合并到master分支，提示某文件合并冲突
- 手动修改冲突的文件去掉注释，然后提交 git commit -m “merge”
- git log 查看比较feature 的提交和 master的提交
- 补充：git checkout -b feature创建新分支同时切换到新分支
- 补充：git branch hotfix feature  基于feature分支创建一个hotfix分支（保留了历史提交版本）

# 13. git merge的三种方式
- 当前分支和历史版本
    - master：a -> b -> c -> d
    - feature: e -> f

1. fast-forward(缺省 git merge)：a -> b -> c -> d -> e -> f
2. --no-ff：a -> b -> c -> d -> e -> f -> g
3. --squash：a -> b -> c -> d  即e和f的改变合并到d版本中

# 14. git创建分支，删除，重命名分支
- master分支随时可以发布，开发分支是dev分支，开发新功能还要另起一个feature分支，功能开发完再合并到dev分支;
- 如果master出现了bug，则需要起一个bugfix分支，在bugfix分支上修复后，再合并到master和dev分支上。
- git init后需要先提交一次，生成master分支，才能创建别的分支
- git branch branchname       创建分支
- git branch -v
- git branch -m branchkname newname  分支重命名
- git checkout branchname     切换到指定分支
- git checkout -b brachname   创建分支同时切换到新分支
- git branch -d brachname     删除分支
- -d删除分支时，如果该分支有提交但没有合并到你所在的分支，则会提示用-D强制删除。

# 15. 远程分支创建，删除，拉取，重命名，覆盖
    ```
            ------------push-------  ----commit----[Index]<--add----
            V                     |  V                              |
        [Remote]--fetch/clone-->[Repository]----checkout---->[Workspace]
            |                                                    A
            -----------------pull---------------------------------
    
    
                               ------1. fork------>
             [A's github repo]                      [B's github repo]
                               <--6. pull request--
                                                        |     A
                                                    2.clone   |
                                                        |   5. push                                                
                                                        V     |
                                                     [B's local repo]
                                                        
                                                        3. modify code
                                                        4. commit
    
    ```
- git pull = git fetch + git merge
- 远程仓库操作
    1. 向远程仓库增加分支
    2. 拉取远程分支
    3. 删除远程分支
    4. 重命名远程分支
    5. 用远程分支强制覆盖本地分支
    ```
    mkdir test && cd test
    git init
    git remote add origin git@github.com:SJ5J/test.git
    git remote -v
    git status
    git add a
    git commit -m "a"
    
    //在master分支上创建feature分支，首先在本地仓库创建分支，再提交远程仓库
    git checkout -b feature
    git status
    git add b 
    git commit -m "b"
    git log --oneline
    git push -u origin feature
    
    //拉取，pull = fetch + merge
    git checkout master
    git status
    git pull origin master
    git log --oneline  //可看到本地库与远程库一致，即feature分支与master分支合并
    
    //删除远程分支
    git push origin --delete feature
    
    //重命名
    1. 先删除该远程分支
    2. 重命名本地分支 git branch -m 
    3. 向远程仓库增加分支
    
    //覆盖本地分支
    git push origin master
    git reset --hard FETCH_HEAD
    git log --oneline
    ```
# 16. git merge和git rebase的区别
- merge会保留完整的提交历史记录：各分支的提交记录和合并记录;
- rebase则把所有提交记录算在当前分支上，且不保留合并记录。
    ```
    git init
    git add a
    git commit -m "a"
    
    git checkout -b feature
    git add b
    git commit -m "b"
    
    git checkout master
    git add c
    git commit -m "c"
    
    //当前feature版本
    b
    a
    
    //当前master版本
    c
    a
    
    //先用merge合并，结果为
    git merge feature
    git log --oneline
    git log --graph --oneline  可显示出是两条分支合并的结果
    
    merge test
    c
    b
    a
    
    //回到原来的master（a，c）状态，再用rebase合并
    git reset --head cID
    git rebase feature
    git log --oneline          与merge的区别是没有merge合并的那次提交，只有合并后的
    git log --graph --oneline  与merge的区别是 只显示出一条分支
    
    c
    b
    a
    
    ```

# 17. 解决git rebase合并中出现的冲突
    ```
    如果master和feature都对同一文件做了不同修改，合并时会提示冲突
    git rebase feature  
    git status
    先手动修改冲突文件，去掉注释
    
    git add 冲突文件
    git status
    
    git rebase --continue
    git status
    
    ```

# 18. git新开发的功能推送到远程仓库
- 拉取远程仓库代码 git pull origin master
- 创建新分支feature，并开发新功能
- 将自己的代码（feature分支）合并到master分支
- 将master分支推送到远程仓库
    ```
    git remote add origin git@github.com:SJ5J/test.git
    git pull origin master
    git checkout -b feature
    修改代码
    git add 修改的文件
    git commit -m "new feature"
    
    git checkout master
    git merge feature
    git push origin master
    ```

# 19. git stash
- 你正在开发新代码，而且暂存区和本地库都有代码。
- 这时发现线上有个bug，需要停止手上的开发。
- 此时，需用git stash 保存暂存区和工作区的代码到当前的开发分支
- 然后，创建新分支，修复bug
- 重新将stash里的代码拉取出来
    ```
    //当前分支为newdev
    git stash
    
    //发现bug，创建新分支用来修复bug
    git checkout -b hotfix
    修改。。。
    
    //切回原来的开发分支，取出stash保存的代码（默认取出暂存区和工作区，加--index只取出暂存区）
    git checkout newdev
    git stash pop 
    ```

# 20. git cherry-pick
- 场景：
- 如果你在某个分支开发代码，然后别人在另外的分支开发。
- 现在别人已经提交了代码，你想要合并别人的某几次提交或某一次提交。
    ```
    git init
    git add a
    git commit -m "a"
    
    git checkout -b hotfix
    git add b
    git commit -m "b"
    
    git add c
    git commit -m "c"
    
    git checkout master
    
    //只想要c那次提交
    git cherry-pick cID...
    git log --oneline
    ```

# 21. git format-patch以及冲突的解决
- 生成补丁patch，补丁是两次（版本）提交之间差异（diff）
- 生成补丁 git format-patch commitID -o 指定目录
- 检查补丁是否可用 git apply --check *.patch
- 冲突后生成rej冲突文件 git apply --reject *.patch 
- 应用补丁，生成am应用环境 git am *.patch
- 场景：
    - master： a -> b
    - feature: a -> b - > c
    - 将feature分支的bc两次的差异生成补丁patch，然后master使用补丁来合并c的提交。
    - 一般用于团队外部人员，做了很小的修改，但没权限直接修改远程仓库，用patch更简单方便（比pull request）。

- 解决冲突：
    - --check发现冲突后，先应用一下生成am应用环境 git am *.patch
    - 再用git apply --reject *.patch 会生成 .rej文件   
    - 根据rej文件手动修改相应冲突文件，然后git add 冲突文件
    - 最后 git am --resolved
    - git status

