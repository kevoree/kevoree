package org.kevoree.library.javase.fileSystemGitRepository;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.javase.fileSystem.api.AbstractItem;
import org.kevoree.library.javase.fileSystem.api.FolderItem;
import org.kevoree.library.javase.fileSystem.api.LockFilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


@Requires({
		@RequiredPort(name = "gitManagement", type = PortType.SERVICE, className = GitRepositoryActions.class, optional = true, needCheckDependency = true),
		@RequiredPort(name = "files", type = PortType.SERVICE, className = LockFilesService.class, optional = true, needCheckDependency = true)
})
@ComponentType
public class FileSystemGitRepositoryRequest extends AbstractComponentType {
	private Logger logger = LoggerFactory.getLogger(FileSystemGitRepositoryRequest.class);
	private MyFrame frame = null;

	@Start
	public void start () {
		frame = new MyFrame();
		frame.setVisible(true);
	}

	@Stop
	public void stop () {
		frame.dispose();
		frame = null;
	}

	private class MyFrame extends JFrame {

		private JButton createRepo, importRepo;
		private String login;
		private String password;
		private String nameRepository;

		String pathRepository;

		public MyFrame () {


			login = "AccountTest";
			password = "AccountTest1";
			nameRepository = "createRepositoryTest" + System.currentTimeMillis();
			pathRepository = "/tmp/";

			createRepo = new JButton("initRepo");
			createRepo.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed (ActionEvent e) {
					if (isPortBinded("createRepo")) {
						AbstractItem root = getPortByName("createRepo", GitRepositoryActions.class).initRepository(login, password, nameRepository, pathRepository);
						logger.debug(" namùe " + root.getName() + " " + root.getPath());
					}
				}
			});


			importRepo = new JButton("cloneRepo");
			importRepo.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed (ActionEvent e) {
					if (isPortBinded("createRepo")) {
						nameRepository = "GL";
						getPortByName("createRepo", GitRepositoryActions.class).importRepository(login, password, "https://" + login + "@github.com/" + login +
								"/" + nameRepository + ".git", nameRepository, pathRepository);

						AbstractItem absItem = new FolderItem();
						absItem.setName(pathRepository + nameRepository);
						AbstractItem root = getPortByName("files", LockFilesService.class).getArborecence(absItem);

						FolderItem realRoot = (FolderItem) ((FolderItem) root).getChilds().get(0);

						System.err.println(realRoot.getChilds().get(4).getName() + " - " + realRoot.getChilds().get(4).getPath());

						System.err.println(" bool " + getPortByName("files", LockFilesService.class).delete(realRoot.getChilds().get(4).getPath()));

						getPortByName("files", LockFilesService.class).mkdirs("/tototutu/tititutu/trope");
						getPortByName("files", LockFilesService.class).saveFile("/tototutu/tititutu/trope/tryc.txt", "toto".getBytes(), true);

						getPortByName("files", LockFilesService.class).move("/tototutu/tititutu/trope/tryc.txt", "/tryc.xml");
						// getPortByName("createRepo", GitRepositoryActions.class).
					}
				}
			});

			/*
					   editorField.addTextListener(new TextListener() {
						   @Override
						   public void textValueChanged(TextEvent e) {
							   getPortByName("createRepo", GitRepositoryActions.class)
									   .updateContentFileAndCommit("/tmp/TestFile.txt", editorField.getText().getBytes(), login);
						   }
					   });
						*/

			setLayout(new FlowLayout());
			add(createRepo);
			add(importRepo);


			this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

			pack();
			setVisible(true);
		}
	}
}

