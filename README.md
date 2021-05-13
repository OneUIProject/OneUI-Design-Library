# SamsungDesign
Samsung OneUI design for AndroidStudio

Install:
1)
`allprojects {
    repositories {
        ...
        maven {
            url "https://jitpack.io"
            credentials { username authToken }
        }
    }
}`

2)
`dependencies {
    implementation 'com.github.Yanndroid:SamsungDesign:1.0.0'
    ...
}`

3)
`<application
        ...
        android:theme="@style/SamsungTheme"
        >
        ...
    </application>`
